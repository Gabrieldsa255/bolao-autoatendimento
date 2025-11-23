package br.com.bolao.entity;

public class Mesa {

    private Integer idMesa;
    private Integer numero;
    private String status;      // LIVRE, OCUPADA, etc.
    private String observacao;

    public Mesa() {
    }

    public Mesa(Integer idMesa, Integer numero, String status, String observacao) {
        this.idMesa = idMesa;
        this.numero = numero;
        this.status = status;
        this.observacao = observacao;
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Integer idMesa) {
        this.idMesa = idMesa;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        return "Mesa{" +
                "idMesa=" + idMesa +
                ", numero=" + numero +
                ", status='" + status + '\'' +
                ", observacao='" + observacao + '\'' +
                '}';
    }
}
