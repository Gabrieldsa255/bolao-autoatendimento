package br.com.bolao.dao;

import br.com.bolao.entity.Mesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    public List<Mesa> listarTodas() {
        List<Mesa> mesas = new ArrayList<>();

        String sql = "SELECT id_mesa, numero, status, observacao FROM mesa ORDER BY numero";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Mesa mesa = new Mesa();
                mesa.setIdMesa(rs.getInt("id_mesa"));
                mesa.setNumero(rs.getInt("numero"));
                mesa.setStatus(rs.getString("status"));
                mesa.setObservacao(rs.getString("observacao"));
                mesas.add(mesa);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar mesas:");
            e.printStackTrace();
        }

        return mesas;
    }

    public Mesa buscarPorNumero(int numero) {
        String sql = "SELECT id_mesa, numero, status, observacao FROM mesa WHERE numero = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Mesa mesa = new Mesa();
                    mesa.setIdMesa(rs.getInt("id_mesa"));
                    mesa.setNumero(rs.getInt("numero"));
                    mesa.setStatus(rs.getString("status"));
                    mesa.setObservacao(rs.getString("observacao"));
                    return mesa;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar mesa por número:");
            e.printStackTrace();
        }

        return null;
    }
}
