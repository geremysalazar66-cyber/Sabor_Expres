package sabor.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sabor.dao.MesaDAO;
import sabor.dao.PagoDAO;
import sabor.dao.PedidoDAO;
import sabor.dao.ReporteDAO;
import sabor.db.Conexion;
import sabor.model.Estados;
import sabor.model.ItemPedido;
import sabor.model.PagoReg;
import sabor.model.Pedido;
import sabor.model.ResumenDia;
import sabor.model.Usuario;
import sabor.util.Fmt;
import sabor.util.PdfSimple;

/** Patrón Facade: una sola llamada registra el pago, cierra el pedido, libera la mesa y emite la boleta. */
public class CajaFacade {
    private final PedidoDAO pedidos = new PedidoDAO();
    private final PagoDAO pagos = new PagoDAO();
    private final MesaDAO mesas = new MesaDAO();
    private final ReporteDAO reportes = new ReporteDAO();

    public record Boleta(String texto, double vuelto) {}

    public Boleta cobrar(Pedido p, String metodo, double recibido) throws SQLException {
        double total = p.total;
        if (Estados.EFECTIVO.equals(metodo)) {
            if (recibido + 0.001 < total) throw new IllegalArgumentException("Dinero insuficiente: faltan " + Fmt.soles(total - recibido));
        } else {
            recibido = total; // tarjeta o Yape/Plin: se cobra el monto exacto
        }
        double vuelto = Math.max(0, recibido - total);

        Connection c = Conexion.getInstancia().get();
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            pedidos.marcarCobrado(c, p.id);
            pagos.registrar(c, p.id, total, metodo);
            mesas.actualizarEstado(c, p.idMesa, Estados.LIBRE);
            c.commit();
        } catch (SQLException | RuntimeException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
        PedidoNotifier.notificar();
        return new Boleta(texto(p, metodo, recibido, vuelto), vuelto);
    }

    private String texto(Pedido p, String metodo, double recibido, double vuelto) {
        StringBuilder sb = new StringBuilder();
        sb.append("        SABOR EXPRESS\n     BOLETA DE VENTA\n");
        sb.append("--------------------------------\n");
        sb.append(String.format("Orden: %s   Mesa: %d%n", p.codigo(), p.numeroMesa));
        sb.append("Fecha: ").append(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        sb.append("--------------------------------\n");
        for (ItemPedido i : p.items) {
            sb.append(String.format("%-18s %2d x %6.2f %7.2f%n", cortar(i.nombre, 18), i.cantidad, i.precio, i.subtotal()));
        }
        sb.append("--------------------------------\n");
        sb.append(String.format("TOTAL:            %s%n", Fmt.soles(p.total)));
        sb.append(String.format("Método:           %s%n", Estados.metodoTexto(metodo)));
        if (Estados.EFECTIVO.equals(metodo)) {
            sb.append(String.format("Recibido:         %s%n", Fmt.soles(recibido)));
            sb.append(String.format("Vuelto:           %s%n", Fmt.soles(vuelto)));
        }
        sb.append("\n   ¡Gracias por su visita!\n");
        return sb.toString();
    }

    private static String cortar(String s, int n) { return s.length() <= n ? s : s.substring(0, n); }

    /** Genera el informe PDF de cierre del día y deja registro en la tabla reportes. */
    public void generarInformePdf(LocalDate dia, Usuario autor, File destino) throws SQLException, IOException {
        ResumenDia r = reportes.resumen(dia);
        List<PagoReg> lista = pagos.delDia(dia);
        List<String> l = new ArrayList<>();
        l.add("# SABOR EXPRESS - Reporte de Cierre de Caja");
        l.add("Fecha: " + Fmt.fechaLarga(dia));
        l.add("Generado por: " + autor.nombre());
        l.add("");
        l.add("# Resumen de ventas");
        l.add("Ventas brutas del día: " + Fmt.soles(r.ventas()));
        l.add("N° de comandas cerradas: " + r.pedidos());
        l.add("Boleta promedio: " + Fmt.soles(r.ticketPromedio()));
        l.add("Mayor venta individual: " + Fmt.soles(r.mayor()));
        l.add("Pagos en efectivo: " + Fmt.soles(r.efectivo()));
        l.add("Pagos con tarjeta: " + Fmt.soles(r.tarjeta()));
        l.add("Pagos con Yape/Plin: " + Fmt.soles(r.yape()));
        l.add("");
        l.add("# Registro de comandas pagadas");
        if (lista.isEmpty()) l.add("No se registraron ventas en la fecha seleccionada.");
        for (PagoReg p : lista) {
            l.add(String.format("%s   Mesa %d   %s   %s   %s", p.codigo(), p.numeroMesa(), Fmt.hora(p.hora()),
                    Fmt.soles(p.monto()), Estados.metodoTexto(p.metodo())));
        }
        PdfSimple.escribir(destino, l);
        reportes.guardar(autor.id(), dia, r.pedidos(), r.ventas());
    }
}
