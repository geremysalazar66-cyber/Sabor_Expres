package sabor.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import sabor.dao.PagoDAO;
import sabor.dao.PedidoDAO;
import sabor.dao.ReporteDAO;
import sabor.model.Estados;
import sabor.model.PagoReg;
import sabor.model.Pedido;
import sabor.model.ResumenDia;
import sabor.model.Usuario;
import sabor.service.CajaFacade;

public class CajaController {
    private final PedidoDAO pedidos = new PedidoDAO();
    private final PagoDAO pagos = new PagoDAO();
    private final ReporteDAO reportes = new ReporteDAO();
    private final CajaFacade facade = new CajaFacade();

    public List<Pedido> listosParaCobro() throws SQLException { return pedidos.listarPorEstados(Estados.LISTO); }
    public List<PagoReg> pagosRecientes() throws SQLException { return pagos.recientes(5); }
    public List<PagoReg> pagosDelDia(LocalDate d) throws SQLException { return pagos.delDia(d); }
    public ResumenDia resumen(LocalDate d) throws SQLException { return reportes.resumen(d); }

    public CajaFacade.Boleta cobrar(Pedido p, String metodo, double recibido) throws SQLException {
        return facade.cobrar(p, metodo, recibido);
    }

    public void generarPdf(LocalDate d, Usuario autor, File f) throws SQLException, IOException {
        facade.generarInformePdf(d, autor, f);
    }
}
