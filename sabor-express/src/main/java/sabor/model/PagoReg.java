package sabor.model;
import java.time.LocalDateTime;
import sabor.util.Fmt;
public record PagoReg(int idPedido, int numeroMesa, LocalDateTime hora, double monto, String metodo) {
    public String codigo() { return Fmt.codigo(idPedido); }
}
