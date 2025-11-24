package br.com.bolao;

import br.com.bolao.dao.*;
import br.com.bolao.entity.ItemPedido;
import br.com.bolao.entity.Mesa;
import br.com.bolao.entity.Pedido;
import br.com.bolao.entity.Produto;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        MesaDAO mesaDAO = new MesaDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        PedidoDAO pedidoDAO = new PedidoDAO();
        ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();
        PagamentoDAO pagamentoDAO = new PagamentoDAO();

        int opcaoPrincipal;

        do {
            System.out.println("\n==== SISTEMA BOLÃO SANTA TERESA ====");
            System.out.println("1 - Totem de autoatendimento (cliente)");
            System.out.println("2 - Módulo da cozinha");
            System.out.println("3 - Módulo garçom / gerente");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            opcaoPrincipal = lerInteiro(scanner);

            switch (opcaoPrincipal) {
                case 1:
                    executarTotem(scanner, mesaDAO, produtoDAO, pedidoDAO, itemPedidoDAO, pagamentoDAO);
                    break;
                case 2:
                    executarModuloCozinha(scanner, pedidoDAO, itemPedidoDAO);
                    break;
                case 3:
                    executarModuloGarcom(scanner, mesaDAO, produtoDAO, pedidoDAO, itemPedidoDAO);
                    break;
                case 0:
                    System.out.println("Encerrando sistema...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcaoPrincipal != 0);

        scanner.close();
    }


    // MÓDULO TOTEM (cliente)

    private static void executarTotem(Scanner scanner,
                                      MesaDAO mesaDAO,
                                      ProdutoDAO produtoDAO,
                                      PedidoDAO pedidoDAO,
                                      ItemPedidoDAO itemPedidoDAO,
                                      PagamentoDAO pagamentoDAO) {

        int opcao;

        do {
            System.out.println("\n==== TOTEM BOLÃO SANTA TERESA ====");
            System.out.println("1 - Fazer pedido");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");
            opcao = lerInteiro(scanner);

            switch (opcao) {
                case 1:
                    menuTipoPedido(scanner, mesaDAO, produtoDAO, pedidoDAO, itemPedidoDAO, pagamentoDAO);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    // escolhe se o pedido é mesa ou retirada
    private static void menuTipoPedido(Scanner scanner,
                                       MesaDAO mesaDAO,
                                       ProdutoDAO produtoDAO,
                                       PedidoDAO pedidoDAO,
                                       ItemPedidoDAO itemPedidoDAO,
                                       PagamentoDAO pagamentoDAO) {

        System.out.println("\nQual tipo de pedido você deseja?");
        System.out.println("1 - Pedido para MESA");
        System.out.println("2 - Pedido para RETIRADA");
        System.out.println("0 - Cancelar");
        System.out.print("Escolha uma opção: ");
        int opcao = lerInteiro(scanner);

        switch (opcao) {
            case 1:
                fluxoPedidoMesaTotem(scanner, mesaDAO, produtoDAO, pedidoDAO, itemPedidoDAO, pagamentoDAO);
                break;
            case 2:
                fluxoPedidoRetirada(scanner, produtoDAO, pedidoDAO, itemPedidoDAO, pagamentoDAO);
                break;
            case 0:
                System.out.println("Pedido cancelado.");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    // pedido
    private static void fluxoPedidoMesaTotem(Scanner scanner,
                                             MesaDAO mesaDAO,
                                             ProdutoDAO produtoDAO,
                                             PedidoDAO pedidoDAO,
                                             ItemPedidoDAO itemPedidoDAO,
                                             PagamentoDAO pagamentoDAO) {

        System.out.println("\nMesas disponíveis e status:");
        listarMesas(mesaDAO);

        System.out.print("\nDigite o número da mesa: ");
        int numeroMesa = lerInteiro(scanner);


        Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(numeroMesa);

        if (pedido == null) {
            // não existe sessão -> cria uma nova
            pedido = pedidoDAO.criarPedidoMesa(numeroMesa, null);
            if (pedido == null) {

                return;
            }
            System.out.println("✅ Pedido aberto para a mesa " + numeroMesa +
                    ". Código do pedido: " + pedido.getIdPedido());
        } else {

            System.out.println("+ Adicionando itens ao pedido existente "
                    + pedido.getIdPedido() + " da mesa " + numeroMesa);
        }

        // fluxo de itens (COMIDA / BEBIDA)
        adicionarItensPorCategoria(scanner, produtoDAO, itemPedidoDAO, pedido);

        //  valida e fecha fluxo do totem
        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getIdPedido());
        if (itens.isEmpty()) {
            System.out.println("⚠ Nenhum item adicionado. Pedido não será enviado para a cozinha.");
            return;
        }

        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());
        imprimirNotaFinal(pedido, itens, total);
        registrarPagamentoTotem(scanner, pedido, total, pagamentoDAO);

        System.out.println("🍳 Pedido enviado para a cozinha!");
    }

    // fluxo pedido RETIRADA + pagamento fictício
    private static void fluxoPedidoRetirada(Scanner scanner,
                                            ProdutoDAO produtoDAO,
                                            PedidoDAO pedidoDAO,
                                            ItemPedidoDAO itemPedidoDAO,
                                            PagamentoDAO pagamentoDAO) {

        System.out.println("\nIniciando pedido de RETIRADA.");
        System.out.print("Informe um nome ou telefone para identificar o pedido (ou deixe em branco): ");
        scanner.nextLine(); // limpa \n pendente do último nextInt
        String observacaoCliente = scanner.nextLine();
        if (observacaoCliente.isBlank()) {
            observacaoCliente = null;
        }

        Pedido pedido = pedidoDAO.criarPedidoRetirada(observacaoCliente);
        if (pedido == null) {
            return;
        }

        System.out.println("✅ Pedido de retirada criado. Código: " + pedido.getIdPedido());

        // fluxo de itens
        adicionarItensPorCategoria(scanner, produtoDAO, itemPedidoDAO, pedido);

        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getIdPedido());
        if (itens.isEmpty()) {
            System.out.println("⚠ Nenhum item adicionado. Pedido não será enviado para a cozinha.");
            return;
        }

        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());
        imprimirNotaFinal(pedido, itens, total);
        registrarPagamentoTotem(scanner, pedido, total, pagamentoDAO);

        System.out.println(" Pedido de retirada enviado para a cozinha!");

    }


    // Adiciona itens ao pedido separando comida e bebida

    private static void adicionarItensPorCategoria(Scanner scanner,
                                                   ProdutoDAO produtoDAO,
                                                   ItemPedidoDAO itemPedidoDAO,
                                                   Pedido pedido) {

        boolean adicionando = true;

        while (adicionando) {
            System.out.println("\nAdicionar itens ao pedido " + pedido.getIdPedido());
            System.out.println("1 - Ver COMIDAS e adicionar");
            System.out.println("2 - Ver BEBIDAS e adicionar");
            System.out.println("0 - Finalizar pedido");
            System.out.print("Escolha uma opção: ");
            int opc = lerInteiro(scanner);

            if (opc == 0) {

                adicionando = false;
                break;
            }

            String categoria;
            if (opc == 1) {
                categoria = "COMIDA";
            } else if (opc == 2) {
                categoria = "BEBIDA";
            } else {
                System.out.println("Opção inválida.");
                continue;
            }

            // Busca só os produtos daquela categoria
            List<Produto> produtosCategoria = produtoDAO.listarAtivosPorCategoria(categoria);

            if (produtosCategoria.isEmpty()) {
                System.out.println("Não há produtos cadastrados na categoria " + categoria + ".");
                continue;
            }


            mostrarCardapio(produtosCategoria);

            System.out.print("\nDigite o ID do produto (0 para voltar ao menu de COMIDA/BEBIDA): ");
            int idProdutoEscolhido = lerInteiro(scanner);

            if (idProdutoEscolhido == 0) {

                continue;
            }

            Produto produtoSelecionado = buscarProdutoPorId(produtosCategoria, idProdutoEscolhido);
            if (produtoSelecionado == null) {
                System.out.println("❌ Produto não encontrado.");
                continue;
            }

            System.out.print("Quantidade: ");
            int quantidade = lerInteiro(scanner);

            itemPedidoDAO.adicionarItem(
                    pedido.getIdPedido(),
                    produtoSelecionado.getIdProduto(),
                    quantidade,
                    produtoSelecionado.getPreco(),
                    null
            );

            System.out.println("✅ Item adicionado ao pedido!");
        }
    }

    // Funções de apoio (mesas, cardápio, impressão, pagamento)

    private static void listarMesas(MesaDAO mesaDAO) {
        List<Mesa> mesas = mesaDAO.listarTodas();
        System.out.println("\n🪑 Mesas cadastradas no sistema:");
        for (Mesa m : mesas) {
            System.out.println("Mesa " + m.getNumero() + " - " + m.getStatus());
        }
    }

    // Cardápio do banco de dados
    private static void mostrarCardapio(ProdutoDAO produtoDAO) {
        List<Produto> produtos = produtoDAO.listarAtivos();
        mostrarCardapio(produtos);
    }


    private static void mostrarCardapio(List<Produto> produtos) {
        System.out.println("\n🍽️ Cardápio:");
        for (Produto p : produtos) {
            System.out.println(p.getIdProduto() + " - " + p.getCategoria() + " - " + p.getNome() +
                    " (R$ " + p.getPreco() + ") - " + p.getDescricao());
        }
    }


    private static Produto buscarProdutoPorId(List<Produto> produtos, int idProduto) {
        for (Produto p : produtos) {
            if (p.getIdProduto() == idProduto) {
                return p;
            }
        }
        return null;
    }

    private static void imprimirNotaFinal(Pedido pedido,
                                          List<ItemPedido> itens,
                                          double total) {
        System.out.println("\n===== RESUMO DO PEDIDO =====");
        System.out.println("Código do pedido: " + pedido.getIdPedido());
        if ("MESA".equalsIgnoreCase(pedido.getTipoAtendimento())) {
            System.out.println("Atendimento: MESA " + pedido.getIdMesa());
        } else {
            System.out.println("Atendimento: RETIRADA");
        }
        System.out.println("----------------------------");
        for (ItemPedido item : itens) {
            String nome = item.getNomeProduto() != null
                    ? item.getNomeProduto()
                    : "Produto " + item.getIdProduto();
            System.out.printf("%dx %s - R$ %.2f%n",
                    item.getQuantidade(),
                    nome,
                    item.getValorTotal());
        }
        System.out.println("----------------------------");
        System.out.printf("TOTAL: R$ %.2f%n", total);
    }

    // PAGAMENTO FICTÍCIO
    private static void registrarPagamentoTotem(Scanner scanner,
                                                Pedido pedido,
                                                double total,
                                                PagamentoDAO pagamentoDAO) {

        System.out.println("\nEscolha a forma de pagamento:");
        System.out.println("1 - DINHEIRO");
        System.out.println("2 - CARTÃO");
        System.out.println("3 - PIX");
        System.out.print("Opção: ");
        int op = lerInteiro(scanner);

        String forma;
        switch (op) {
            case 1:
                forma = "DINHEIRO";
                break;
            case 2:
                forma = "CARTAO";
                break;
            case 3:
                forma = "PIX";
                break;
            default:
                forma = "OUTRO";
                System.out.println("Opção inválida, registrando como OUTRO.");
                break;
        }

        System.out.println("\n💳 Processando pagamento no totem...");
        System.out.printf("Forma: %s | Valor cobrado: R$ %.2f%n", forma, total);

        // grava na tabela pagamento
        pagamentoDAO.registrarPagamento(pedido.getIdPedido(), forma, total);

        System.out.println("✅ Pagamento confirmado no totem.");
    }


    // MÓDULO COZINHA

    private static void executarModuloCozinha(Scanner scanner,
                                              PedidoDAO pedidoDAO,
                                              ItemPedidoDAO itemPedidoDAO) {

        int opcao;

        do {
            System.out.println("\n==== MÓDULO DA COZINHA ====");
            System.out.println("1 - Listar pedidos pendentes");
            System.out.println("2 - Ver detalhes de um pedido");
            System.out.println("3 - Atualizar status de um pedido");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");
            opcao = lerInteiro(scanner);

            switch (opcao) {
                case 1:
                    listarPedidosCozinha(pedidoDAO);
                    break;
                case 2:
                    verDetalhesPedido(scanner, pedidoDAO, itemPedidoDAO);
                    break;
                case 3:
                    atualizarStatusPedidoCozinha(scanner, pedidoDAO);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    private static void listarPedidosCozinha(PedidoDAO pedidoDAO) {
        List<Pedido> pedidos = pedidoDAO.listarPedidosCozinha();

        if (pedidos.isEmpty()) {
            System.out.println("\n📣 Não há pedidos pendentes na cozinha.");
            return;
        }

        System.out.println("\n🍳 Pedidos para a cozinha:");
        for (Pedido p : pedidos) {
            String infoMesa = (p.getIdMesa() != null) ? "Mesa " + p.getIdMesa() : "RETIRADA";
            System.out.println("ID " + p.getIdPedido() +
                    " | " + infoMesa +
                    " | Status: " + p.getStatus() +
                    " | Aberto em: " + p.getDataHoraAbertura());
        }
    }

    private static void verDetalhesPedido(Scanner scanner,
                                          PedidoDAO pedidoDAO,
                                          ItemPedidoDAO itemPedidoDAO) {

        System.out.print("\nDigite o ID do pedido: ");
        int id = lerInteiro(scanner);

        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(id);
        if (itens.isEmpty()) {
            System.out.println("Nenhum item encontrado para esse pedido.");
            return;
        }

        double total = pedidoDAO.calcularTotalPedido(id);

        System.out.println("\nItens do pedido " + id + ":");
        for (ItemPedido item : itens) {
            String nome = item.getNomeProduto() != null
                    ? item.getNomeProduto()
                    : "Produto " + item.getIdProduto();
            System.out.printf("%dx %s - R$ %.2f%n",
                    item.getQuantidade(),
                    nome,
                    item.getValorTotal());
        }
        System.out.printf("Total atual: R$ %.2f%n", total);
    }

    private static void atualizarStatusPedidoCozinha(Scanner scanner,
                                                     PedidoDAO pedidoDAO) {

        System.out.print("\nDigite o ID do pedido para atualizar (0 para voltar): ");
        int idEscolhido = lerInteiro(scanner);
        if (idEscolhido == 0) {
            return;
        }

        System.out.println("1 - Marcar como EM_PREPARACAO");
        System.out.println("2 - Marcar como PRONTO");
        System.out.print("Escolha: ");
        int opcaoStatus = lerInteiro(scanner);

        String novoStatus;
        if (opcaoStatus == 1) {
            novoStatus = "EM_PREPARACAO";
        } else if (opcaoStatus == 2) {
            novoStatus = "PRONTO";
        } else {
            System.out.println("Opção inválida.");
            return;
        }


        pedidoDAO.atualizarStatusPedido(idEscolhido, novoStatus);
        System.out.println("✅ Pedido " + idEscolhido + " atualizado para " + novoStatus + ".");
    }


    // MÓDULO GARÇOM / GERENTE

    private static void executarModuloGarcom(Scanner scanner,
                                             MesaDAO mesaDAO,
                                             ProdutoDAO produtoDAO,
                                             PedidoDAO pedidoDAO,
                                             ItemPedidoDAO itemPedidoDAO) {

        int opcao;

        do {
            System.out.println("\n==== MÓDULO GARÇOM / GERENTE ====");
            System.out.println("1 - Listar mesas e status");
            System.out.println("2 - Trabalhar em uma mesa");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");
            opcao = lerInteiro(scanner);

            switch (opcao) {
                case 1:
                    listarMesas(mesaDAO);
                    break;
                case 2:
                    fluxoGarcomTrabalharMesa(scanner, mesaDAO, produtoDAO, pedidoDAO, itemPedidoDAO);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    // Garçom escolhe uma mesa e entra em um atendimento só dela
    private static void fluxoGarcomTrabalharMesa(Scanner scanner,
                                                 MesaDAO mesaDAO,
                                                 ProdutoDAO produtoDAO,
                                                 PedidoDAO pedidoDAO,
                                                 ItemPedidoDAO itemPedidoDAO) {

        System.out.println("\nMesas e status:");
        listarMesas(mesaDAO);

        System.out.print("\nDigite o número da mesa para atender: ");
        int numeroMesa = lerInteiro(scanner);

        boolean continuarNaMesa = true;

        while (continuarNaMesa) {
            System.out.println("\n=== ATENDIMENTO MESA " + numeroMesa + " ===");
            System.out.println("1 - Ver resumo da mesa");
            System.out.println("2 - Adicionar itens");
            System.out.println("3 - Ver conta parcial");
            System.out.println("4 - Liberar mesa");
            System.out.println("0 - Voltar às mesas");
            System.out.print("Escolha uma opção: ");
            int opcaoMesa = lerInteiro(scanner);

            switch (opcaoMesa) {
                case 1:
                    verResumoMesaNumero(numeroMesa, pedidoDAO, itemPedidoDAO);
                    break;
                case 2:
                    adicionarItensMesaGarcom(scanner, numeroMesa, produtoDAO, pedidoDAO, itemPedidoDAO);
                    break;
                case 3:
                    verContaParcialMesa(numeroMesa, pedidoDAO);
                    break;
                case 4:
                    boolean liberada = liberarMesaGarcom(scanner, numeroMesa, pedidoDAO, itemPedidoDAO);
                    if (liberada) {
                        continuarNaMesa = false; // saiu da mesa
                    }
                    break;
                case 0:
                    continuarNaMesa = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    // Ver resumo da mesa
    private static void verResumoMesaNumero(int numeroMesa,
                                            PedidoDAO pedidoDAO,
                                            ItemPedidoDAO itemPedidoDAO) {

        Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(numeroMesa);
        if (pedido == null) {
            System.out.println("Nenhuma comanda ativa encontrada para a mesa " + numeroMesa + ".");
            return;
        }

        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getIdPedido());
        if (itens.isEmpty()) {
            System.out.println("Não há itens lançados para essa mesa.");
            return;
        }

        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());
        imprimirNotaFinal(pedido, itens, total);
    }


    private static void adicionarItensMesaGarcom(Scanner scanner,
                                                 int numeroMesa,
                                                 ProdutoDAO produtoDAO,
                                                 PedidoDAO pedidoDAO,
                                                 ItemPedidoDAO itemPedidoDAO) {

        // Busca pedido ativo
        Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(numeroMesa);

        if (pedido == null) {

            pedido = pedidoDAO.criarPedidoMesa(numeroMesa, "Comanda aberta pelo garçom");
            if (pedido == null) {
                return;
            }
            System.out.println("✅ Nova comanda aberta para a mesa " + numeroMesa +
                    ". Código do pedido: " + pedido.getIdPedido());
        } else {
            System.out.println("➕ Adicionando itens ao pedido existente "
                    + pedido.getIdPedido() + " da mesa " + numeroMesa);
        }


        adicionarItensPorCategoria(scanner, produtoDAO, itemPedidoDAO, pedido);

        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());
        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getIdPedido());
        if (!itens.isEmpty()) {
            System.out.println("\nResumo atualizado da mesa " + numeroMesa + ":");
            imprimirNotaFinal(pedido, itens, total);
        }
    }


    private static void verContaParcialMesa(int numeroMesa,
                                            PedidoDAO pedidoDAO) {

        Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(numeroMesa);
        if (pedido == null) {
            System.out.println("Nenhuma comanda ativa encontrada para a mesa " + numeroMesa + ".");
            return;
        }

        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());
        System.out.printf("💰 Conta parcial da mesa %d: R$ %.2f%n", numeroMesa, total);
    }

    // Liberação manual da mesa pelo garçom / gerente
    private static boolean liberarMesaGarcom(Scanner scanner,
                                             int numeroMesa,
                                             PedidoDAO pedidoDAO,
                                             ItemPedidoDAO itemPedidoDAO) {

        // Busca o pedido
        Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(numeroMesa);
        if (pedido == null) {
            System.out.println("Nenhum pedido ativo encontrado para a mesa " + numeroMesa + ".");
            return false;
        }

        if ("EM_PREPARACAO".equalsIgnoreCase(pedido.getStatus())) {
            System.out.println("⚠ Atenção: ainda há itens EM PREPARAÇÃO para esta mesa.");
        }

        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getIdPedido());
        double total = pedidoDAO.calcularTotalPedido(pedido.getIdPedido());

        if (!itens.isEmpty()) {
            System.out.println("\nSituação atual da mesa " + numeroMesa + ":");
            imprimirNotaFinal(pedido, itens, total);
        }

        System.out.print("Confirmar liberação da mesa " + numeroMesa +
                " (cliente já saiu)? (1 = Sim, 2 = Não): ");
        int conf = lerInteiro(scanner);
        if (conf != 1) {
            System.out.println("Liberação cancelada.");
            return false;
        }


        pedidoDAO.atualizarStatusPedido(pedido.getIdPedido(), "ENTREGUE");

        System.out.println("✅ Mesa " + numeroMesa + " liberada para um novo cliente.");
        return true;
    }


    private static int lerInteiro(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Digite um número válido: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
