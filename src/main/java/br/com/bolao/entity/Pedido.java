package br.com.bolao.entity;

import java.time.LocalDateTime;

public class Pedido {

    private Integer idPedido;
    private String tipoAtendimento;       // MESA, RETIRADA
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private String status;                // ABERTO, EM_PREPARACAO, PRONTO, ENTREGUE...
    private Integer idMesa;
    private Integer idCliente;
    private String origem;                // TOTEM, GARCOM, IFOOD
    private String observacao;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Integer idMesa) {
        this.idMesa = idMesa;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", tipoAtendimento='" + tipoAtendimento + '\'' +
                ", dataHoraAbertura=" + dataHoraAbertura +
                ", status='" + status + '\'' +
                ", idMesa=" + idMesa +
                ", origem='" + origem + '\'' +
                '}';
    }
}
