// src/main/java/br/com/bolao/dao/PagamentoDAO.java
package br.com.bolao.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PagamentoDAO {

    public void registrarPagamento(int idPedido,
                                   String formaPagamento,
                                   double valorPago) {

        String sql =
                "INSERT INTO pagamento " +
                        "(id_pedido, forma_pagamento, valor_pago, data_hora_pagamento, status) " +
                        "VALUES (?, ?, ?, NOW(), 'PAGO')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.setString(2, formaPagamento);
            ps.setDouble(3, valorPago);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
