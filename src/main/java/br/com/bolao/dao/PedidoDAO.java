package br.com.bolao.dao;

import br.com.bolao.entity.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Pedido – compatível com o Main antigo e com as melhorias novas.
 */
public class PedidoDAO {

    /**
     * Busca pedido ABERTO para uma mesa específica (pode retornar null se não existir).
     */
    public Pedido buscarPedidoAbertoPorMesa(int numeroMesa) {
        String sql =
                "SELECT p.* " +
                        "FROM pedido p " +
                        "JOIN mesa m ON p.id_mesa = m.id_mesa " +
                        "WHERE m.numero = ? AND p.status = 'ABERTO' " +
                        "ORDER BY p.data_hora_abertura DESC " +
                        "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numeroMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cria pedido do tipo MESA, já com observação opcional.
     * Também marca a mesa como OCUPADA.
     */
    public Pedido criarPedidoMesa(int numeroMesa, String observacao) {
        String sqlInsert =
                "INSERT INTO pedido (tipo_atendimento, data_hora_abertura, status, id_mesa, origem, observacao) " +
                        "VALUES ('MESA', NOW(), 'ABERTO', (SELECT id_mesa FROM mesa WHERE numero = ?), 'TOTEM', ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int idPedidoGerado;

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, numeroMesa);
                ps.setString(2, observacao);
                int linhas = ps.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Nenhuma linha inserida ao criar pedido de mesa.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPedidoGerado = rs.getInt(1);
                    } else {
                        throw new SQLException("Não foi possível obter o ID gerado do pedido de mesa.");
                    }
                }
            }

            // Marca a mesa como OCUPADA
            try (PreparedStatement psMesa =
                         conn.prepareStatement("UPDATE mesa SET status = 'OCUPADA' WHERE numero = ?")) {
                psMesa.setInt(1, numeroMesa);
                psMesa.executeUpdate();
            }

            conn.commit();

            Pedido p = new Pedido();
            p.setIdPedido(idPedidoGerado);
            p.setTipoAtendimento("MESA");
            p.setStatus("ABERTO");
            p.setObservacao(observacao);
            // aqui usamos o número da mesa; no banco há id_mesa, mas para o Main isso é suficiente
            p.setIdMesa(numeroMesa);
            p.setOrigem("TOTEM");
            return p;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Cria pedido do tipo RETIRADA, com observação (nome/telefone, etc.).
     */
    public Pedido criarPedidoRetirada(String observacao) {
        String sql =
                "INSERT INTO pedido (tipo_atendimento, data_hora_abertura, status, origem, observacao) " +
                        "VALUES ('RETIRADA', NOW(), 'ABERTO', 'TOTEM', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, observacao);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    Pedido p = new Pedido();
                    p.setIdPedido(id);
                    p.setTipoAtendimento("RETIRADA");
                    p.setStatus("ABERTO");
                    p.setObservacao(observacao);
                    p.setOrigem("TOTEM");
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Atualiza apenas a observação de um pedido existente.
     */
    public void atualizarObservacao(int idPedido, String observacao) {
        String sql = "UPDATE pedido SET observacao = ? WHERE id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, observacao);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Atualiza o status do pedido.
     * Ex.: ABERTO, EM_PREPARACAO, PRONTO, ENTREGUE, CANCELADO.
     * Se marcar ENTREGUE ou CANCELADO, preenche data_hora_fechamento.
     */
    public void atualizarStatusPedido(int idPedido, String novoStatus) {
        String sql =
                "UPDATE pedido " +
                        "SET status = ?, " +
                        "    data_hora_fechamento = CASE " +
                        "        WHEN ? IN ('ENTREGUE','CANCELADO') THEN NOW() " +
                        "        ELSE data_hora_fechamento " +
                        "    END " +
                        "WHERE id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, novoStatus);
            ps.setString(2, novoStatus);
            ps.setInt(3, idPedido);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Nome usado no Main antigo
    public void atualizarStatus(int idPedido, String novoStatus) {
        atualizarStatusPedido(idPedido, novoStatus);
    }

    /**
     * Fecha o pedido (marca como ENTREGUE) e libera a mesa (status LIVRE).
     * Usado em liberarMesaGarcom no Main.
     */
    public void fecharPedido(int idPedido) {
        // 1) Marca pedido como ENTREGUE )
        atualizarStatusPedido(idPedido, "ENTREGUE");

        // 2) Libera a mesa ligada a esse pedido
        String sqlMesa =
                "UPDATE mesa m " +
                        "JOIN pedido p ON p.id_mesa = m.id_mesa " +
                        "SET m.status = 'LIVRE', m.observacao = NULL " +
                        "WHERE p.id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMesa)) {

            ps.setInt(1, idPedido);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // LISTAGENS PARA COZINHA

    private List<Pedido> listarPedidosCozinhaBase() {
        List<Pedido> lista = new ArrayList<>();

        String sql =
                "SELECT p.id_pedido, p.tipo_atendimento, p.status, p.observacao, " +
                        "       p.data_hora_abertura, p.data_hora_fechamento, p.origem, p.id_mesa " +
                        "FROM pedido p " +
                        "WHERE p.status IN ('ABERTO','EM_PREPARACAO','PRONTO') " +
                        "ORDER BY p.data_hora_abertura";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = mapearPedido(rs);
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /** Nome chamado no seu Main (console). */
    public List<Pedido> listarPedidosCozinha() {
        return listarPedidosCozinhaBase();
    }

    /** Nome que usei em algumas respostas para o JavaFX. */
    public List<Pedido> listarPedidosParaCozinha() {
        return listarPedidosCozinhaBase();
    }

    /**
     * Calcula o total de um pedido somando os itens no banco.
     * Mantém compatibilidade com o Main antigo.
     */
    public double calcularTotalPedido(Integer idPedido) {
        String sql =
                "SELECT COALESCE(SUM(valor_total), 0) AS total " +
                        "FROM item_pedido " +
                        "WHERE id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Pedido

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();

        try {
            p.setIdPedido(rs.getInt("id_pedido"));
        } catch (SQLException ignored) {}

        try {
            p.setTipoAtendimento(rs.getString("tipo_atendimento"));
        } catch (SQLException ignored) {}

        try {
            p.setStatus(rs.getString("status"));
        } catch (SQLException ignored) {}

        try {
            p.setObservacao(rs.getString("observacao"));
        } catch (SQLException ignored) {}

        try {
            p.setOrigem(rs.getString("origem"));
        } catch (SQLException ignored) {}

        // id_mesa pode ser nulo
        try {
            int idMesa = rs.getInt("id_mesa");
            if (!rs.wasNull()) {
                p.setIdMesa(idMesa);
            }
        } catch (SQLException ignored) {}

        try {
            Timestamp tsAbertura = rs.getTimestamp("data_hora_abertura");
            if (tsAbertura != null) {
                p.setDataHoraAbertura(LocalDateTime.ofInstant(
                        tsAbertura.toInstant(), ZoneId.systemDefault()));
            }
        } catch (SQLException ignored) {}

        try {
            Timestamp tsFech = rs.getTimestamp("data_hora_fechamento");
            if (tsFech != null) {
                p.setDataHoraFechamento(LocalDateTime.ofInstant(
                        tsFech.toInstant(), ZoneId.systemDefault()));
            }
        } catch (SQLException ignored) {}

        return p;
    }
}
