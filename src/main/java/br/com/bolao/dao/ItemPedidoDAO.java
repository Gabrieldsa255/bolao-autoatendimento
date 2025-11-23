package br.com.bolao.dao;

import br.com.bolao.entity.ItemPedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ItemPedidoDAO {

    /**
     * Adiciona um item ao pedido.
     *
     * @param idPedido      pedido ao qual o item pertence
     * @param idProduto     produto escolhido
     * @param quantidade    quantidade
     * @param valorUnitario valor unitário no momento do pedido
     * @param observacao    observação opcional (pode ser null)
     */
    public void adicionarItem(int idPedido,
                              int idProduto,
                              int quantidade,
                              double valorUnitario,
                              String observacao) {

        String sql =
                "INSERT INTO item_pedido " +
                        "  (id_pedido, id_produto, quantidade, valor_unitario, valor_total, observacao) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        double valorTotal = quantidade * valorUnitario;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idProduto);
            ps.setInt(3, quantidade);
            ps.setDouble(4, valorUnitario);
            ps.setDouble(5, valorTotal);
            ps.setString(6, observacao);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lista todos os itens de um pedido.
     */
    public List<ItemPedido> listarPorPedido(int idPedido) {
        List<ItemPedido> itens = new ArrayList<>();

        String sql =
                "SELECT ip.id_produto, ip.quantidade, ip.valor_total, " +
                        "       p.nome AS nome_produto " +
                        "FROM item_pedido ip " +
                        "JOIN produto p ON p.id_produto = ip.id_produto " +
                        "WHERE ip.id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemPedido item = new ItemPedido();

                    try {
                        item.setIdProduto(rs.getInt("id_produto"));
                    } catch (SQLException ignored) {}

                    try {
                        item.setQuantidade(rs.getInt("quantidade"));
                    } catch (SQLException ignored) {}

                    try {
                        item.setValorTotal(rs.getDouble("valor_total"));
                    } catch (SQLException ignored) {}

                    try {
                        item.setNomeProduto(rs.getString("nome_produto"));
                    } catch (SQLException ignored) {}

                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }
}
