package br.com.bolao.dao;

import br.com.bolao.entity.ItemPedido;

import java.sql.*;
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

            System.out.printf(
                    "[ItemPedidoDAO] Inserindo item -> pedido=%d, produto=%d, qtd=%d, unit=%.2f, total=%.2f%n",
                    idPedido, idProduto, quantidade, valorUnitario, valorTotal
            );

            ps.setInt(1, idPedido);
            ps.setInt(2, idProduto);
            ps.setInt(3, quantidade);
            ps.setDouble(4, valorUnitario);
            ps.setDouble(5, valorTotal);

            if (observacao == null || observacao.isBlank()) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, observacao);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Erro ao inserir item_pedido:");
            e.printStackTrace();
        }
    }

    /**
     * Lista todos os itens de um pedido.
     */
    public List<ItemPedido> listarPorPedido(int idPedido) {
        List<ItemPedido> itens = new ArrayList<>();

        String sql =
                "SELECT ip.id_produto, ip.quantidade, ip.valor_unitario, ip.valor_total, " +
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

                    item.setIdProduto(rs.getInt("id_produto"));
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setValorUnitario(rs.getDouble("valor_unitario"));
                    item.setValorTotal(rs.getDouble("valor_total"));
                    item.setNomeProduto(rs.getString("nome_produto"));

                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar itens do pedido:");
            e.printStackTrace();
        }

        return itens;
    }
}
