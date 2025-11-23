package br.com.bolao;

import br.com.bolao.dao.*;
import br.com.bolao.entity.ItemPedido;
import br.com.bolao.entity.Mesa;
import br.com.bolao.entity.Pedido;
import br.com.bolao.entity.Produto;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BolaoApp extends Application {

    // Paleta principal
    private static final String COR_FUNDO = "#1F3C34";    // verde escuro
    private static final String COR_CREME = "#FCECC0";    // creme
    private static final String COR_LARANJA = "#C96A2C";  // laranja
    private static final String COR_VERDE_MESA = "#3B8E3C";
    private static final String COR_VERMELHO_MESA = "#C0392B";

    // DAOs compartilhados
    private final MesaDAO mesaDAO = new MesaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        mostrarMenuPrincipal();
    }

    // =========================================================
    // MENU PRINCIPAL
    // =========================================================
    private void mostrarMenuPrincipal() {

        // Cabeçalho com logo + textos
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);

        ImageView logoView = carregarLogo();
        VBox textos = new VBox(5);
        textos.setAlignment(Pos.CENTER_LEFT);

        Label lblBolao = new Label("Bolão Santa Teresa");
        lblBolao.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        lblBolao.setTextFill(Color.web(COR_CREME));

        Label lblSub = new Label("Sistema de autoatendimento");
        lblSub.setFont(Font.font("Georgia", FontWeight.NORMAL, 18));
        lblSub.setTextFill(Color.web(COR_CREME));

        textos.getChildren().addAll(lblBolao, lblSub);

        if (logoView != null) {
            header.getChildren().addAll(logoView, textos);
        } else {
            header.getChildren().add(textos);
        }

        Button btnTotem = criarBotaoMenu("TOTEM / CLIENTE");
        Button btnCozinha = criarBotaoMenu("MÓDULO COZINHA");
        Button btnGarcom = criarBotaoMenu("MÓDULO GARÇOM / GERENTE");

        btnTotem.setOnAction(e -> abrirFluxoTotemCliente());
        btnCozinha.setOnAction(e ->
                mostrarMensagemTema("Módulo Cozinha",
                        "Tela da cozinha ainda em desenvolvimento visual.\n\n" +
                                "A lógica de pedidos já está pronta nos DAOs, " +
                                "podemos ligar tudo depois.")
        );
        btnGarcom.setOnAction(e ->
                mostrarMensagemTema("Módulo Garçom / Gerente",
                        "Tela do garçom/gerente ainda em desenvolvimento visual.\n\n" +
                                "Ela irá mostrar as mesas, consumo parcial e botões para liberar mesas.")
        );

        VBox root = new VBox(40, header, btnTotem, btnCozinha, btnGarcom);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 80, 40, 80));
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Sistema Bolão Santa Teresa");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ImageView carregarLogo() {
        try {
            // Coloque sua logo em: src/main/resources/img/logo-bolao.png
            InputStream is = getClass().getResourceAsStream("/img/logo-bolao.png");
            if (is == null) {
                return null;
            }
            Image img = new Image(is);
            ImageView iv = new ImageView(img);
            iv.setFitHeight(80);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private Button criarBotaoMenu(String texto) {
        Button b = new Button(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(60);
        b.setStyle(
                "-fx-background-color: #F4D9A0;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2F2F2F;" +
                        "-fx-background-radius: 18;"
        );
        VBox.setMargin(b, new Insets(0, 120, 0, 120));
        return b;
    }

    // =========================================================
    // TOTEM / CLIENTE - TELA 1
    // =========================================================
    private void abrirFluxoTotemCliente() {
        Label titulo = new Label("Bolão - Totem");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setTextFill(Color.web(COR_CREME));

        Button btnMesa = new Button("Comer no salão (Mesa)");
        Button btnRetirada = new Button("Retirada no balcão");
        estilizarBotaoPrimario(btnMesa);
        estilizarBotaoSecundario(btnRetirada);

        btnMesa.setOnAction(e -> abrirTelaEscolhaMesa());
        btnRetirada.setOnAction(e -> abrirTelaPedidoRetirada());

        VBox root = new VBox(40, titulo, btnMesa, btnRetirada);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
    }

    private void estilizarBotaoPrimario(Button b) {
        b.setMaxWidth(260);
        b.setPrefHeight(70);
        b.setStyle(
                "-fx-background-color: " + COR_CREME + ";" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2F2F2F;" +
                        "-fx-background-radius: 18;"
        );
    }

    private void estilizarBotaoSecundario(Button b) {
        b.setMaxWidth(260);
        b.setPrefHeight(70);
        b.setStyle(
                "-fx-background-color: " + COR_LARANJA + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 18;"
        );
    }

    // =========================================================
    // TOTEM / CLIENTE - ESCOLHER MESA (cards verdes/vermelhos)
    // =========================================================
    private void abrirTelaEscolhaMesa() {
        List<Mesa> mesas = mesaDAO.listarTodas();

        Label titulo = new Label("Escolha uma mesa");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web(COR_CREME));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        ToggleGroup grupoMesas = new ToggleGroup();

        int col = 0;
        int row = 0;
        for (Mesa mesa : mesas) {
            ToggleButton tb = criarCardMesa(mesa);
            tb.setToggleGroup(grupoMesas);
            grid.add(tb, col, row);

            col++;
            if (col == 5) { // 5 colunas por linha
                col = 0;
                row++;
            }
        }

        Button btnContinuar = new Button("Abrir / continuar pedido");
        btnContinuar.setStyle(
                "-fx-background-color: #F4D9A0;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 16;"
        );
        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> mostrarMenuPrincipal());

        HBox botoes = new HBox(15, btnVoltar, btnContinuar);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(10, 0, 20, 0));

        btnContinuar.setOnAction(e -> {
            ToggleButton selec = (ToggleButton) grupoMesas.getSelectedToggle();
            if (selec == null) {
                mostrarAlerta("Selecione uma mesa.");
                return;
            }
            Mesa mesaSel = (Mesa) selec.getUserData();
            abrirTelaPedidoMesa(mesaSel);
        });

        BorderPane root = new BorderPane();
        VBox topo = new VBox(20, titulo);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(20, 0, 0, 0));

        root.setTop(topo);
        root.setCenter(grid);
        root.setBottom(botoes);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
    }

    private ToggleButton criarCardMesa(Mesa mesa) {
        ToggleButton tb = new ToggleButton(String.valueOf(mesa.getNumero()));
        tb.setPrefSize(100, 70);
        tb.setUserData(mesa);

        tb.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                atualizarEstiloMesa(tb, mesa));

        atualizarEstiloMesa(tb, mesa);
        return tb;
    }

    private void atualizarEstiloMesa(ToggleButton tb, Mesa mesa) {
        String corFundo = "LIVRE".equalsIgnoreCase(mesa.getStatus())
                ? COR_VERDE_MESA
                : COR_VERMELHO_MESA;

        String corBorda = tb.isSelected() ? "#FFD54F" : "transparent";

        String style =
                "-fx-background-radius: 18;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COR_CREME + ";" +
                        "-fx-background-color: " + corFundo + ";" +
                        "-fx-border-color: " + corBorda + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 18;";

        tb.setStyle(style);
    }

    // =========================================================
    // TOTEM / CLIENTE - TELA DE PRODUTOS (mesa)
    // =========================================================
    private void abrirTelaPedidoMesa(Mesa mesa) {
        Label titulo = new Label("Mesa " + mesa.getNumero());
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web(COR_CREME));

        Label subtitulo = new Label("Selecione os itens para esta mesa");
        subtitulo.setFont(Font.font("System", 14));
        subtitulo.setTextFill(Color.web(COR_CREME));

        // produto -> quantidade
        Map<Produto, IntegerProperty> mapaQuantidades = new LinkedHashMap<>();

        Label lblTotal = new Label("Total: R$ 0,00");
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(COR_CREME));

        Runnable atualizarTotal = () -> {
            double total = calcularTotal(mapaQuantidades);
            lblTotal.setText(String.format("Total: R$ %.2f", total));
        };

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
                "-fx-tab-min-width: 120; -fx-tab-min-height: 40;" +
                        "-fx-font-size: 14px;"
        );
        Tab tabComidas = new Tab("Comidas");
        Tab tabBebidas = new Tab("Bebidas");
        tabs.getTabs().addAll(tabComidas, tabBebidas);

        tabComidas.setContent(criarListaProdutosPorCategoria("COMIDA", mapaQuantidades, atualizarTotal));
        tabBebidas.setContent(criarListaProdutosPorCategoria("BEBIDA", mapaQuantidades, atualizarTotal));

        Button btnConfirmar = new Button("Confirmar pedido");
        estilizarBotaoConfirmar(btnConfirmar);

        Button btnVoltar = new Button("Voltar mesas");
        estilizarBotaoCancelar(btnVoltar);
        btnVoltar.setOnAction(e -> abrirTelaEscolhaMesa());

        HBox barraInferior = new HBox(20, btnVoltar, lblTotal, btnConfirmar);
        barraInferior.setAlignment(Pos.CENTER_RIGHT);
        barraInferior.setPadding(new Insets(15));
        HBox.setHgrow(lblTotal, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        VBox topo = new VBox(4, titulo, subtitulo);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.setPadding(new Insets(20, 20, 10, 20));
        root.setTop(topo);
        root.setCenter(tabs);
        root.setBottom(barraInferior);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);

        // AÇÃO CONFIRMAR
        btnConfirmar.setOnAction(e -> {
            boolean temItens = mapaQuantidades.values().stream()
                    .anyMatch(q -> q.get() > 0);

            if (!temItens) {
                mostrarAlerta("Nenhum item selecionado.");
                return;
            }

            double totalCarrinho = calcularTotal(mapaQuantidades);

            String formaPagamento = abrirDialogoPagamento(totalCarrinho);
            if (formaPagamento == null) {
                mostrarMensagemTema("Pagamento não concluído",
                        "Você cancelou o pagamento. O pedido não foi enviado para a cozinha.");
                return;
            }

            // Busca ou cria sessão de pedido
            Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());
            if (pedido == null) {
                pedido = pedidoDAO.criarPedidoMesa(mesa.getNumero(), null);
                if (pedido == null) {
                    mostrarAlerta("Não foi possível abrir pedido para a mesa.");
                    return;
                }
            }

            salvarItensNoBanco(pedido, mapaQuantidades);
            pagamentoDAO.registrarPagamento(pedido.getIdPedido(), formaPagamento, totalCarrinho);

            mostrarMensagemTema(
                    "Pedido confirmado",
                    String.format(
                            "Mesa %d\nValor pago: R$ %.2f\nForma de pagamento: %s\n\n" +
                                    "Seu pedido foi enviado para a cozinha.",
                            mesa.getNumero(), totalCarrinho, formaPagamento
                    )
            );

            abrirTelaEscolhaMesa();
        });
    }

    // =========================================================
    // LISTA DE PRODUTOS POR CATEGORIA (mesa e retirada)
    // =========================================================
    private VBox criarListaProdutosPorCategoria(String categoria,
                                                Map<Produto, IntegerProperty> mapaQuantidades,
                                                Runnable onChangeTotal) {

        List<Produto> produtos = produtoDAO.listarAtivosPorCategoria(categoria);

        VBox container = new VBox(10);
        container.setPadding(new Insets(15));

        String corCard = categoria.equalsIgnoreCase("COMIDA")
                ? "#264A3F"
                : "#1E3C5A";

        String corChip = categoria.equalsIgnoreCase("COMIDA")
                ? "#F4D9A0"
                : "#89CFF0";

        for (Produto p : produtos) {
            IntegerProperty qtdProp =
                    mapaQuantidades.computeIfAbsent(p, k -> new SimpleIntegerProperty(0));

            Label lblNome = new Label(p.getNome());
            lblNome.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label lblDesc = new Label(p.getDescricao());
            lblDesc.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 12px;");
            lblDesc.setWrapText(true);

            Label lblPreco = new Label(String.format("R$ %.2f", p.getPreco()));
            lblPreco.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label chipCategoria = new Label(
                    categoria.equalsIgnoreCase("COMIDA") ? "Prato" : "Bebida"
            );
            chipCategoria.setStyle(
                    "-fx-background-color: " + corChip + ";" +
                            "-fx-text-fill: #2F2F2F;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 2 8 2 8;"
            );

            Label lblQtd = new Label();
            lblQtd.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 16px;");
            lblQtd.textProperty().bind(qtdProp.asString());

            Button btnMenos = new Button("-");
            Button btnMais = new Button("+");
            btnMenos.setPrefSize(32, 32);
            btnMais.setPrefSize(32, 32);

            btnMenos.setStyle("-fx-background-radius: 16;");
            btnMais.setStyle("-fx-background-radius: 16;");

            btnMenos.setOnAction(e -> {
                int v = qtdProp.get();
                if (v > 0) qtdProp.set(v - 1);
                onChangeTotal.run();
            });
            btnMais.setOnAction(e -> {
                qtdProp.set(qtdProp.get() + 1);
                onChangeTotal.run();
            });

            HBox linhaQtd = new HBox(10, btnMenos, lblQtd, btnMais);
            linhaQtd.setAlignment(Pos.CENTER_RIGHT);

            VBox caixaTexto = new VBox(4, lblNome, lblDesc, lblPreco);
            HBox topLine = new HBox(10, caixaTexto, chipCategoria);
            topLine.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(caixaTexto, Priority.ALWAYS);

            VBox esquerda = new VBox(6, topLine);
            HBox card = new HBox(20, esquerda, linhaQtd);
            card.setPadding(new Insets(12));
            card.setStyle(
                    "-fx-background-color: " + corCard + ";" +
                            "-fx-background-radius: 12;"
            );
            card.setAlignment(Pos.CENTER_LEFT);

            container.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + COR_FUNDO + "; -fx-border-color: transparent;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    // =========================================================
    // TOTEM / CLIENTE - PEDIDO RETIRADA
    // =========================================================
    private void abrirTelaPedidoRetirada() {
        Label titulo = new Label("Retirada no balcão");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setTextFill(Color.web(COR_CREME));

        // Cartão com nome/telefone do cliente
        Label lblPergunta = new Label("Como podemos chamar você?");
        lblPergunta.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblPergunta.setTextFill(Color.web("#2F2F2F"));

        Label lblHint = new Label("Informe um nome ou número de telefone para identificarmos seu pedido.");
        lblHint.setFont(Font.font("System", 12));
        lblHint.setTextFill(Color.web("#2F2F2F"));
        lblHint.setWrapText(true);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Ex.: Gabriel, (31) 99999-0000...");
        txtNome.setPrefWidth(320);

        VBox cardCliente = new VBox(8, lblPergunta, lblHint, txtNome);
        cardCliente.setPadding(new Insets(15));
        cardCliente.setStyle(
                "-fx-background-color: " + COR_CREME + ";" +
                        "-fx-background-radius: 14;"
        );

        // Carrinho
        Map<Produto, IntegerProperty> mapaQuantidades = new LinkedHashMap<>();

        Label lblTotal = new Label("Total: R$ 0,00");
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(COR_CREME));

        Runnable atualizarTotal = () -> {
            double total = calcularTotal(mapaQuantidades);
            lblTotal.setText(String.format("Total: R$ %.2f", total));
        };

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
                "-fx-tab-min-width: 120; -fx-tab-min-height: 40;" +
                        "-fx-font-size: 14px;"
        );
        Tab tabComidas = new Tab("Comidas");
        Tab tabBebidas = new Tab("Bebidas");
        tabs.getTabs().addAll(tabComidas, tabBebidas);

        tabComidas.setContent(criarListaProdutosPorCategoria("COMIDA", mapaQuantidades, atualizarTotal));
        tabBebidas.setContent(criarListaProdutosPorCategoria("BEBIDA", mapaQuantidades, atualizarTotal));

        Button btnConfirmar = new Button("Ir para pagamento");
        estilizarBotaoConfirmar(btnConfirmar);
        Button btnVoltar = new Button("Voltar");
        estilizarBotaoCancelar(btnVoltar);
        btnVoltar.setOnAction(e -> abrirFluxoTotemCliente());

        HBox barraInferior = new HBox(20, btnVoltar, lblTotal, btnConfirmar);
        barraInferior.setAlignment(Pos.CENTER_RIGHT);
        barraInferior.setPadding(new Insets(15));

        VBox topo = new VBox(15, titulo, cardCliente);
        topo.setPadding(new Insets(20, 20, 10, 20));
        topo.setSpacing(10);

        BorderPane root = new BorderPane();
        root.setTop(topo);
        root.setCenter(tabs);
        root.setBottom(barraInferior);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);

        btnConfirmar.setOnAction(e -> {
            boolean temItens = mapaQuantidades.values().stream()
                    .anyMatch(q -> q.get() > 0);

            if (!temItens) {
                mostrarAlerta("Nenhum item selecionado.");
                return;
            }

            double totalCarrinho = calcularTotal(mapaQuantidades);
            String formaPagamento = abrirDialogoPagamento(totalCarrinho);
            if (formaPagamento == null) {
                mostrarMensagemTema("Pagamento não concluído",
                        "Você cancelou o pagamento. O pedido não foi enviado para a cozinha.");
                return;
            }

            String obs = txtNome.getText();
            if (obs != null && obs.isBlank()) {
                obs = null;
            }

            Pedido pedido = pedidoDAO.criarPedidoRetirada(obs);
            if (pedido == null) {
                mostrarAlerta("Não foi possível criar o pedido de retirada.");
                return;
            }

            salvarItensNoBanco(pedido, mapaQuantidades);
            pagamentoDAO.registrarPagamento(pedido.getIdPedido(), formaPagamento, totalCarrinho);

            String nomeMostrado = (obs == null || obs.isBlank())
                    ? String.valueOf(pedido.getIdPedido())
                    : obs;

            mostrarMensagemTema(
                    "Pedido de retirada confirmado",
                    String.format(
                            "Código do pedido: %d\nCliente: %s\nValor pago: R$ %.2f\nForma de pagamento: %s\n\n" +
                                    "Retire no balcão quando for chamado.",
                            pedido.getIdPedido(), nomeMostrado, totalCarrinho, formaPagamento
                    )
            );

            abrirFluxoTotemCliente();
        });
    }

    // =========================================================
    // PAGAMENTO (DIÁLOGO ESTILIZADO)
    // =========================================================
    private String abrirDialogoPagamento(double total) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Pagamento");

        Label lblTitulo = new Label("Pagamento");
        lblTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web(COR_CREME));

        Label lblTotal = new Label(String.format("Total a pagar: R$ %.2f", total));
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(COR_CREME));

        Label lblForma = new Label("Forma de pagamento:");
        lblForma.setFont(Font.font("System", 14));
        lblForma.setTextFill(Color.web(COR_CREME));

        ComboBox<String> cbForma = new ComboBox<>();
        cbForma.getItems().addAll("Pix", "Cartão de crédito", "Cartão de débito", "Dinheiro");
        cbForma.getSelectionModel().selectFirst();
        cbForma.setPrefWidth(220);

        HBox linhaForma = new HBox(10, lblForma, cbForma);
        linhaForma.setAlignment(Pos.CENTER_LEFT);

        Button btnOk = new Button("Confirmar");
        estilizarBotaoConfirmar(btnOk);
        Button btnCancel = new Button("Cancelar");
        estilizarBotaoCancelar(btnCancel);

        final String[] resultado = new String[1];

        btnOk.setOnAction(ev -> {
            resultado[0] = cbForma.getValue();
            dialog.close();
        });
        btnCancel.setOnAction(ev -> {
            resultado[0] = null;
            dialog.close();
        });

        HBox botoes = new HBox(15, btnCancel, btnOk);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(20, lblTitulo, lblTotal, linhaForma, botoes);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:" + COR_FUNDO + "; -fx-background-radius: 16;");

        Scene scene = new Scene(root, 400, 220);
        dialog.setScene(scene);
        dialog.showAndWait();

        return resultado[0];
    }

    private void estilizarBotaoConfirmar(Button b) {
        b.setStyle(
                "-fx-background-color: " + COR_LARANJA + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 8 18 8 18;"
        );
    }

    private void estilizarBotaoCancelar(Button b) {
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + COR_CREME + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: " + COR_CREME + ";" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 8 18 8 18;"
        );
    }

    // =========================================================
    // HELPERS: TOTAL / SALVAR ITENS
    // =========================================================
    private double calcularTotal(Map<Produto, IntegerProperty> mapaQuantidades) {
        return mapaQuantidades.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPreco() * e.getValue().get())
                .sum();
    }

    private void salvarItensNoBanco(Pedido pedido,
                                    Map<Produto, IntegerProperty> mapaQuantidades) {
        for (Map.Entry<Produto, IntegerProperty> entry : mapaQuantidades.entrySet()) {
            int qtd = entry.getValue().get();
            if (qtd > 0) {
                Produto prod = entry.getKey();
                itemPedidoDAO.adicionarItem(
                        pedido.getIdPedido(),
                        prod.getIdProduto(),
                        qtd,
                        prod.getPreco(),
                        null
                );
            }
        }
    }

    // =========================================================
    // DIÁLOGOS ESTILIZADOS (ALERTA / INFO)
    // =========================================================
    private void mostrarAlerta(String msg) {
        mostrarMensagemTema("Aviso", msg);
    }

    private void mostrarMensagemTema(String titulo, String mensagem) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(titulo);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        lblTitulo.setTextFill(Color.web(COR_CREME));

        Label lblMsg = new Label(mensagem);
        lblMsg.setWrapText(true);
        lblMsg.setTextFill(Color.web(COR_CREME));
        lblMsg.setFont(Font.font("System", 14));

        Button btnOk = new Button("OK");
        estilizarBotaoConfirmar(btnOk);
        btnOk.setOnAction(ev -> dialog.close());

        HBox linhaBotao = new HBox(btnOk);
        linhaBotao.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, lblTitulo, lblMsg, linhaBotao);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:" + COR_FUNDO + "; -fx-background-radius: 14;");

        Scene scene = new Scene(root, 430, 190);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        launch(args);
    }
}
