// src/main/java/br/com/bolao/dao/ProdutoDAO.java
package br.com.bolao.dao;

import br.com.bolao.entity.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    // =========================================================
    // Lista TODOS os produtos (ativos e inativos)
    // =========================================================
    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();

        String sql = "SELECT id_produto, nome, descricao, categoria, preco, ativo " +
                "FROM produto " +
                "ORDER BY categoria, nome";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = mapearProduto(rs);
                produtos.add(p);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar produtos:");
            e.printStackTrace();
        }

        return produtos;
    }

    // =========================================================
    // Lista apenas produtos ATIVOS (ativo = 1)
    // =========================================================
    public List<Produto> listarAtivos() {
        List<Produto> produtos = new ArrayList<>();

        String sql = "SELECT id_produto, nome, descricao, categoria, preco, ativo " +
                "FROM produto " +
                "WHERE ativo = 1 " +
                "ORDER BY categoria, nome";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = mapearProduto(rs);
                produtos.add(p);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar produtos ativos:");
            e.printStackTrace();
        }

        return produtos;
    }

    // =========================================================
    // Lista somente produtos ATIVOS de uma categoria específica
    //    (por exemplo: "COMIDA" ou "BEBIDA")
    // =========================================================
    public List<Produto> listarAtivosPorCategoria(String categoria) {
        List<Produto> produtos = new ArrayList<>();

        String sql = "SELECT id_produto, nome, descricao, categoria, preco, ativo " +
                "FROM produto " +
                "WHERE ativo = 1 AND categoria = ? " +
                "ORDER BY nome";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto p = mapearProduto(rs);
                    produtos.add(p);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar produtos por categoria:");
            e.printStackTrace();
        }

        return produtos;
    }

    // =========================================================
    // Método auxiliar para montar objeto Produto
    // =========================================================
    private Produto mapearProduto(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setIdProduto(rs.getInt("id_produto"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setCategoria(rs.getString("categoria"));
        p.setPreco(rs.getDouble("preco"));

        // campo "ativo" é Integer na entidade
        p.setAtivo(rs.getInt("ativo"));

        return p;
    }
}
