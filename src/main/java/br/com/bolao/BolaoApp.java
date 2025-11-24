package br.com.bolao;

import br.com.bolao.dao.*;
import br.com.bolao.entity.Mesa;
import br.com.bolao.entity.Pedido;
import br.com.bolao.entity.Produto;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aplicação JavaFX do Sistema Bolão Santa Teresa.
 * Integra TOTEM, Garçom/Gerente e Cozinha usando os DAOs.
 */
public class BolaoApp extends Application {

    // Cores
    private static final String COR_FUNDO = "#1F3C34";      // verde escuro
    private static final String COR_CREME = "#FCECC0";      // creme
    private static final String COR_LARANJA = "#C96A2C";    // laranja

    // Cores mesas totem
    private static final String COR_MESA_LIVRE_CLIENTE = "#3B8E3C";   // verde
    private static final String COR_MESA_OCUPADA_CLIENTE = "#C0392B"; // vermelho

    // Cores cards Garçom
    private static final String COR_CARD_LIVRE = "#3B8E3C";       // verde
    private static final String COR_CARD_OCUPADA = "#D84343";     // vermelho
    private static final String COR_CARD_COM_PEDIDO = "#FFC928";  // amarelo


    private static final String LOGO_RESOURCE = "/bolao-simbolo.png";

    // DAOs
    private final MesaDAO mesaDAO = new MesaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();

    private Stage primaryStage;

    //ajuste de tela
    private double larguraTela;
    private double alturaTela;

    // INÍCIO

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Sistema Bolão Santa Teresa");

        configurarStageTelaCheia();
        mostrarMenuPrincipal();
        primaryStage.show();
    }

    /**
     * Configura o Stage para ocupar a área visual da tela
     * (sem invadir a barra de tarefas).
     */
    private void configurarStageTelaCheia() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        larguraTela = bounds.getWidth();
        alturaTela = bounds.getHeight();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(larguraTela);
        primaryStage.setHeight(alturaTela);
    }

    private Rectangle2D getTelaBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    // MENU PRINCIPAL

    private void mostrarMenuPrincipal() {
        configurarStageTelaCheia();
        Rectangle2D bounds = getTelaBounds();

        // Cabeçalho com logo + textos
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(30, 40, 20, 40));

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
        btnCozinha.setOnAction(e -> abrirModuloCozinha());
        btnGarcom.setOnAction(e -> abrirModuloGarcomGerente());

        VBox botoes = new VBox(30, btnTotem, btnCozinha, btnGarcom);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(20, 120, 60, 120));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(botoes);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);
    }

    private ImageView carregarLogo() {
        try {
            InputStream is = getClass().getResourceAsStream(LOGO_RESOURCE);
            if (is == null) {
                System.out.println("⚠ Logo não encontrada em: " + LOGO_RESOURCE);
                return null;
            }

            Image img = new Image(is);
            ImageView iv = new ImageView(img);
            iv.setFitHeight(80);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            e.printStackTrace();
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


    // TOTEM - TELA 1

    private void abrirFluxoTotemCliente() {
        configurarStageTelaCheia();

        Label titulo = new Label("Bolão - Totem");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setTextFill(Color.web(COR_CREME));

        Button btnMesa = new Button("Comer no salão (Mesa)");
        Button btnRetirada = new Button("Retirada no balcão");
        Button btnVoltar = new Button("Voltar");

        estilizarBotaoPrimario(btnMesa);
        estilizarBotaoSecundario(btnRetirada);
        estilizarBotaoCancelar(btnVoltar);

        btnMesa.setOnAction(e -> abrirTelaEscolhaMesa());
        btnRetirada.setOnAction(e -> abrirTelaPedidoRetirada());
        btnVoltar.setOnAction(e -> mostrarMenuPrincipal());

        VBox opcoes = new VBox(20, btnMesa, btnRetirada);
        opcoes.setAlignment(Pos.CENTER);

        VBox root = new VBox(40, titulo, opcoes, btnVoltar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, larguraTela, alturaTela);
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


    // TOTEM - ESCOLHER MESA

    private void abrirTelaEscolhaMesa() {
        configurarStageTelaCheia();
        Rectangle2D bounds = getTelaBounds();

        List<Mesa> mesas = mesaDAO.listarTodas();

        Label titulo = new Label("Escolha uma mesa");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        titulo.setTextFill(Color.web(COR_CREME));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        ToggleGroup grupoMesas = new ToggleGroup();

        int col = 0;
        int row = 0;
        for (Mesa mesa : mesas) {
            ToggleButton tb = new ToggleButton(String.valueOf(mesa.getNumero()));
            tb.setUserData(mesa);
            tb.setPrefSize(100, 70);

            if (!"LIVRE".equalsIgnoreCase(mesa.getStatus())) {
                tb.setOnAction(e -> {
                    tb.setSelected(false);
                    mostrarAlerta("Mesa ocupada", "Esta mesa está ocupada.\nEscolha outra mesa.");
                });
            }

            tb.selectedProperty().addListener((obs, was, isSel) -> {
                if ("LIVRE".equalsIgnoreCase(mesa.getStatus())) {
                    atualizarEstiloMesaCliente(tb, mesa, isSel);
                } else {
                    atualizarEstiloMesaCliente(tb, mesa, false);
                }
            });
            atualizarEstiloMesaCliente(tb, mesa, false);

            tb.setToggleGroup(grupoMesas);

            grid.add(tb, col, row);
            col++;
            if (col == 5) { // 5 colunas no totem
                col = 0;
                row++;
            }
        }

        // Scroll para garantir que todas as 31 mesas fiquem acessíveis
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background:" + COR_FUNDO + "; -fx-border-color: transparent;");

        Button btnContinuar = new Button("Abrir / continuar pedido");
        btnContinuar.setStyle(
                "-fx-background-color: #F4D9A0;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 16;"
        );

        Button btnVoltar = new Button("Voltar");
        estilizarBotaoCancelar(btnVoltar);
        btnVoltar.setOnAction(e -> abrirFluxoTotemCliente());

        HBox botoes = new HBox(15, btnVoltar, btnContinuar);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(10, 0, 20, 0));

        btnContinuar.setOnAction(e -> {
            ToggleButton selec = (ToggleButton) grupoMesas.getSelectedToggle();
            if (selec == null) {
                mostrarAlerta("Aviso", "Selecione uma mesa.");
                return;
            }
            Mesa mesaSel = (Mesa) selec.getUserData();
            if (!"LIVRE".equalsIgnoreCase(mesaSel.getStatus())) {
                mostrarAlerta("Mesa ocupada", "Esta mesa está ocupada.\nEscolha outra mesa.");
                return;
            }
            abrirTelaPedidoMesa(mesaSel);
        });

        BorderPane root = new BorderPane();
        VBox topo = new VBox(20, titulo);
        topo.setAlignment(Pos.TOP_CENTER);
        topo.setPadding(new Insets(30, 0, 10, 0));

        root.setTop(topo);
        root.setCenter(scroll);
        root.setBottom(botoes);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);
    }

    private void atualizarEstiloMesaCliente(ToggleButton tb, Mesa mesa, boolean selecionada) {
        String corFundo = "LIVRE".equalsIgnoreCase(mesa.getStatus())
                ? COR_MESA_LIVRE_CLIENTE
                : COR_MESA_OCUPADA_CLIENTE;

        String corBorda = selecionada ? "#FFD54F" : "transparent";

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


    // TOTEM - TELA DE PRODUTOS

    private void abrirTelaPedidoMesa(Mesa mesa) {
        configurarStageTelaCheia();
        Rectangle2D bounds = getTelaBounds();

        Label titulo = new Label("Mesa " + mesa.getNumero());
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web(COR_CREME));

        Label subtitulo = new Label("Selecione os itens para esta mesa");
        subtitulo.setFont(Font.font("System", 14));
        subtitulo.setTextFill(Color.web(COR_CREME));

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
        tabs.setStyle("-fx-tab-min-width: 120; -fx-tab-min-height: 40; -fx-font-size: 14px;");

        Tab tabComidas = new Tab("Comidas");
        Tab tabBebidas = new Tab("Bebidas");
        tabComidas.setContent(criarListaProdutosPorCategoria("COMIDA", mapaQuantidades, atualizarTotal));
        tabBebidas.setContent(criarListaProdutosPorCategoria("BEBIDA", mapaQuantidades, atualizarTotal));
        tabs.getTabs().addAll(tabComidas, tabBebidas);

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

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);

        btnConfirmar.setOnAction(e -> {
            boolean temItens = mapaQuantidades.values().stream().anyMatch(q -> q.get() > 0);
            if (!temItens) {
                mostrarAlerta("Aviso", "Nenhum item selecionado.");
                return;
            }

            double totalCarrinho = calcularTotal(mapaQuantidades);
            String formaPagamento = abrirDialogoPagamento(totalCarrinho, mapaQuantidades);
            if (formaPagamento == null) {
                mostrarMensagemTema(
                        "Pagamento não concluído",
                        "Você cancelou o pagamento. O pedido não foi enviado para a cozinha."
                );
                return;
            }

            Pedido pedido = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());
            if (pedido == null) {
                pedido = pedidoDAO.criarPedidoMesa(mesa.getNumero(), null);
                if (pedido == null) {
                    mostrarAlerta("Erro", "Não foi possível abrir pedido para a mesa.");
                    return;
                }
            }

            salvarItensNoBanco(pedido, mapaQuantidades);
            pagamentoDAO.registrarPagamento(pedido.getIdPedido(), formaPagamento, totalCarrinho);

            mostrarMensagemTema(
                    "Pedido confirmado",
                    String.format(
                            "Mesa %d\nValor pago: R$ %.2f\nForma de pagamento: %s\n\nSeu pedido foi enviado para a cozinha.",
                            mesa.getNumero(), totalCarrinho, formaPagamento
                    )
            );
            abrirTelaEscolhaMesa();
        });
    }


    // LISTA DE PRODUTOS

    private VBox criarListaProdutosPorCategoria(String categoria,
                                                Map<Produto, IntegerProperty> mapaQuantidades,
                                                Runnable onChangeTotal) {
        List<Produto> produtos = produtoDAO.listarAtivosPorCategoria(categoria);

        VBox container = new VBox(10);
        container.setPadding(new Insets(15));

        String corCard = categoria.equalsIgnoreCase("COMIDA") ? "#264A3F" : "#1E3C5A";
        String corChip = categoria.equalsIgnoreCase("COMIDA") ? "#F4D9A0" : "#89CFF0";

        for (Produto p : produtos) {
            IntegerProperty qtdProp = mapaQuantidades.computeIfAbsent(p, k -> new SimpleIntegerProperty(0));

            Label lblNome = new Label(p.getNome());
            lblNome.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label lblDesc = new Label(p.getDescricao());
            lblDesc.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 12px;");
            lblDesc.setWrapText(true);

            Label lblPreco = new Label(String.format("R$ %.2f", p.getPreco()));
            lblPreco.setStyle("-fx-text-fill: " + COR_CREME + "; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label chipCategoria = new Label(categoria.equalsIgnoreCase("COMIDA") ? "Prato" : "Bebida");
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
            card.setStyle("-fx-background-color: " + corCard + "; -fx-background-radius: 12;");
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


    // TOTEM - RETIRADA

    private void abrirTelaPedidoRetirada() {
        configurarStageTelaCheia();

        Label titulo = new Label("Retirada no balcão");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setTextFill(Color.web(COR_CREME));

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
        tabs.setStyle("-fx-tab-min-width: 120; -fx-tab-min-height: 40; -fx-font-size: 14px;");

        Tab tabComidas = new Tab("Comidas");
        Tab tabBebidas = new Tab("Bebidas");
        tabComidas.setContent(criarListaProdutosPorCategoria("COMIDA", mapaQuantidades, atualizarTotal));
        tabBebidas.setContent(criarListaProdutosPorCategoria("BEBIDA", mapaQuantidades, atualizarTotal));
        tabs.getTabs().addAll(tabComidas, tabBebidas);

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

        Scene scene = new Scene(root, larguraTela, alturaTela);
        primaryStage.setScene(scene);

        btnConfirmar.setOnAction(e -> {
            boolean temItens = mapaQuantidades.values().stream().anyMatch(q -> q.get() > 0);
            if (!temItens) {
                mostrarAlerta("Aviso", "Nenhum item selecionado.");
                return;
            }

            double totalCarrinho = calcularTotal(mapaQuantidades);
            String formaPagamento = abrirDialogoPagamento(totalCarrinho, mapaQuantidades);
            if (formaPagamento == null) {
                mostrarMensagemTema(
                        "Pagamento não concluído",
                        "Você cancelou o pagamento. O pedido não foi enviado para a cozinha."
                );
                return;
            }

            String obs = txtNome.getText();
            if (obs != null && obs.isBlank()) {
                obs = null;
            }

            Pedido pedido = pedidoDAO.criarPedidoRetirada(obs);
            if (pedido == null) {
                mostrarAlerta("Erro", "Não foi possível criar o pedido de retirada.");
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
                            "Código do pedido: %d\nCliente: %s\nValor pago: R$ %.2f\nForma de pagamento: %s\n\nRetire no balcão quando for chamado.",
                            pedido.getIdPedido(), nomeMostrado, totalCarrinho, formaPagamento
                    )
            );
            abrirFluxoTotemCliente();
        });
    }


    // PAGAMENTO E NOTA"

    private String abrirDialogoPagamento(double total, Map<Produto, IntegerProperty> mapaQuantidades) {
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

        // Notinha
        Label lblResumoTitulo = new Label("Resumo do pedido:");
        lblResumoTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblResumoTitulo.setTextFill(Color.web(COR_CREME));

        VBox boxResumo = new VBox(4);
        boxResumo.setPadding(new Insets(8));
        boxResumo.setStyle(
                "-fx-background-color: #23453B;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + COR_CREME + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        mapaQuantidades.forEach((produto, qtdProp) -> {
            int qtd = qtdProp.get();
            if (qtd > 0) {
                double totalItem = qtd * produto.getPreco();
                Label linha = new Label(
                        String.format("%dx %s - R$ %.2f", qtd, produto.getNome(), totalItem)
                );
                linha.setTextFill(Color.web(COR_CREME));
                linha.setFont(Font.font("System", 13));
                boxResumo.getChildren().add(linha);
            }
        });

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

        VBox root = new VBox(15, lblTitulo, lblTotal, lblResumoTitulo, boxResumo, linhaForma, botoes);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:" + COR_FUNDO + "; -fx-background-radius: 16;");

        Scene scene = new Scene(root, 460, 360);
        dialog.setScene(scene);
        dialog.showAndWait();
        return resultado[0];
    }


    private double calcularTotal(Map<Produto, IntegerProperty> mapaQuantidades) {
        return mapaQuantidades.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPreco() * e.getValue().get())
                .sum();
    }

    private void salvarItensNoBanco(Pedido pedido, Map<Produto, IntegerProperty> mapaQuantidades) {
        mapaQuantidades.forEach((produto, qtdProp) -> {
            int qtd = qtdProp.get();
            if (qtd > 0) {
                itemPedidoDAO.adicionarItem(
                        pedido.getIdPedido(),
                        produto.getIdProduto(),
                        qtd,
                        produto.getPreco(),
                        null
                );
            }
        });
    }

    // ALERTAS

    private void mostrarAlerta(String titulo, String msg) {
        mostrarMensagemTema(titulo, msg);
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

    // MÓDULO GARÇOM / GERENTE

    private void abrirModuloGarcomGerente() {
        configurarStageTelaCheia();
        Rectangle2D bounds = getTelaBounds();

        Label titulo = new Label("Módulo Garçom / Gerente");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        titulo.setTextFill(Color.web(COR_CREME));

        Label subtitulo = new Label("Visão geral das mesas");
        subtitulo.setFont(Font.font("System", 14));
        subtitulo.setTextFill(Color.web(COR_CREME));

        VBox topo = new VBox(5, titulo, subtitulo);
        topo.setAlignment(Pos.TOP_CENTER);
        topo.setPadding(new Insets(20, 0, 30, 0));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setPadding(new Insets(0, 0, 40, 0));

        List<Mesa> mesas = mesaDAO.listarTodas();

        int col = 0;
        int row = 0;
        for (Mesa mesa : mesas) {
            Pedido pedidoAberto = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());
            VBox card = criarCardMesaGarcom(mesa, pedidoAberto);

            grid.add(card, col, row);
            col++;
            if (col == 4) { // 4 colunas
                col = 0;
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background:" + COR_FUNDO + "; -fx-border-color: transparent;");

        Button btnVoltar = new Button("Voltar");
        estilizarBotaoCancelar(btnVoltar);
        btnVoltar.setOnAction(e -> mostrarMenuPrincipal());

        HBox barraInferior = new HBox(btnVoltar);
        barraInferior.setAlignment(Pos.CENTER_RIGHT);
        barraInferior.setPadding(new Insets(0, 30, 20, 0));

        BorderPane root = new BorderPane();
        root.setTop(topo);
        root.setCenter(scroll);
        root.setBottom(barraInferior);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);
    }

    private VBox criarCardMesaGarcom(Mesa mesa, Pedido pedidoAberto) {
        String statusMesa = mesa.getStatus() == null ? "" : mesa.getStatus().toUpperCase();
        boolean temPedido = (pedidoAberto != null);

        String corFundo;
        if ("LIVRE".equalsIgnoreCase(statusMesa)) {
            corFundo = COR_CARD_LIVRE;
        } else if (temPedido) {
            corFundo = COR_CARD_COM_PEDIDO;
        } else {
            corFundo = COR_CARD_OCUPADA;
        }

        Label lblMesa = new Label("Mesa " + mesa.getNumero());
        lblMesa.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMesa.setTextFill(Color.web(COR_CREME));

        Label lblStatus = new Label("Status: " + statusMesa);
        lblStatus.setFont(Font.font("System", 12));
        lblStatus.setTextFill(Color.web(COR_CREME));

        String textoPedido = temPedido
                ? "Pedido aberto #" + pedidoAberto.getIdPedido()
                : "Sem pedido aberto";
        Label lblPedido = new Label(textoPedido);
        lblPedido.setFont(Font.font("System", 12));
        lblPedido.setTextFill(Color.web(COR_CREME));

        Button btnGerenciar = new Button("Gerenciar");
        estilizarBotaoConfirmar(btnGerenciar);
        btnGerenciar.setOnAction(e -> abrirDialogoGerenciarMesa(mesa, pedidoAberto));

        VBox box = new VBox(5, lblMesa, lblStatus, lblPedido, btnGerenciar);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle(
                "-fx-background-color:" + corFundo + ";" +
                        "-fx-background-radius: 14;"
        );
        box.setPrefSize(150, 130);

        return box;
    }

    /**
     * Diálogo de gerenciamento da mesa (Garçom/Gerente).
     * Aqui só existem:
     * - Itens do pedido
     * - Encerrar mesa (fecha pedido se existir ou libera mesa ocupada)
     */
    private void abrirDialogoGerenciarMesa(Mesa mesa, Pedido pedidoAbertoOriginal) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Gerenciar mesa");


        Pedido pedidoAtual = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());

        Label lblTitulo = new Label("Mesa " + mesa.getNumero());
        lblTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        lblTitulo.setTextFill(Color.web(COR_CREME));

        String statusMesa = mesa.getStatus() == null ? "" : mesa.getStatus().toUpperCase();
        Label lblStatus = new Label("Status: " + statusMesa);
        lblStatus.setFont(Font.font("System", 14));
        lblStatus.setTextFill(Color.web(COR_CREME));

        String textoPedido = (pedidoAtual != null)
                ? "Pedido aberto #" + pedidoAtual.getIdPedido()
                : "Sem pedido aberto";
        Label lblPedido = new Label(textoPedido);
        lblPedido.setFont(Font.font("System", 14));
        lblPedido.setTextFill(Color.web(COR_CREME));

        // ITENS DO PEDIDO
        Button btnItens = new Button("Itens do pedido");
        estilizarBotaoConfirmar(btnItens);
        btnItens.setOnAction(e -> {
            Pedido ped = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());
            if (ped == null) {
                mostrarMensagemTema("Sem pedido",
                        "Esta mesa não possui pedido aberto no momento.");
                return;
            }
            String cabecalho = "Mesa " + mesa.getNumero();
            mostrarItensPedidoDetalhado(cabecalho, ped.getIdPedido());
        });

        // ENCERRAR MESA
        Button btnEncerrar = new Button("Encerrar mesa");
        estilizarBotaoConfirmar(btnEncerrar);
        btnEncerrar.setOnAction(e -> {
            Pedido ped = pedidoDAO.buscarPedidoAbertoPorMesa(mesa.getNumero());
            if (ped != null) {
                // Fecha pedido
                pedidoDAO.fecharPedido(ped.getIdPedido());
                // Garante mesa livre
                mesaDAO.atualizarStatusMesa(mesa.getIdMesa(), "LIVRE");
                mostrarMensagemTema("Mesa encerrada",
                        "O pedido #" + ped.getIdPedido() + " foi encerrado e a mesa foi liberada.");
            } else {
                // Sem pedido:liberaR a mesa se estiver marcada como ocupada
                if (!"LIVRE".equalsIgnoreCase(statusMesa)) {
                    mesaDAO.atualizarStatusMesa(mesa.getIdMesa(), "LIVRE");
                    mostrarMensagemTema("Mesa liberada",
                            "A mesa " + mesa.getNumero() + " foi liberada.");
                } else {
                    mostrarMensagemTema("Sem pedido",
                            "Não há pedido aberto nesta mesa para encerrar.");
                }
            }
            dialog.close();
            abrirModuloGarcomGerente();
        });

        Button btnFechar = new Button("Voltar");
        estilizarBotaoCancelar(btnFechar);
        btnFechar.setOnAction(e -> dialog.close());

        HBox linhaBotoes = new HBox(10, btnItens, btnEncerrar, btnFechar);
        linhaBotoes.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, lblTitulo, lblStatus, lblPedido, linhaBotoes);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:" + COR_FUNDO + "; -fx-background-radius: 14;");

        Scene scene = new Scene(root, 520, 230);
        dialog.setScene(scene);
        dialog.showAndWait();
    }


    // MÓDULO COZINHA

    private void abrirModuloCozinha() {
        configurarStageTelaCheia();
        Rectangle2D bounds = getTelaBounds();

        Label titulo = new Label("Módulo Cozinha");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        titulo.setTextFill(Color.web(COR_CREME));

        Label subtitulo = new Label("Pedidos em aberto");
        subtitulo.setFont(Font.font("System", 14));
        subtitulo.setTextFill(Color.web(COR_CREME));

        VBox topo = new VBox(5, titulo, subtitulo);
        topo.setAlignment(Pos.TOP_LEFT);
        topo.setPadding(new Insets(20, 0, 20, 20));

        VBox listaPedidos = new VBox(10);
        listaPedidos.setPadding(new Insets(10));

        // Pedidos para a cozinha
        List<Pedido> pedidos = pedidoDAO.listarPedidosCozinha();


        for (Pedido pedido : pedidos) {
            String status = pedido.getStatus();
            if (status != null && status.equalsIgnoreCase("PRONTO")) {
                continue;
            }
            HBox card = criarCardPedidoCozinha(pedido);
            listaPedidos.getChildren().add(card);
        }

        // Fila livre
        if (listaPedidos.getChildren().isEmpty()) {
            Label lblVazio = new Label("Não há pedidos pendentes no momento.");
            lblVazio.setFont(Font.font("System", FontWeight.BOLD, 18));
            lblVazio.setTextFill(Color.web(COR_CREME));
            listaPedidos.setAlignment(Pos.CENTER);
            listaPedidos.getChildren().add(lblVazio);
        }

        ScrollPane scroll = new ScrollPane(listaPedidos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + COR_FUNDO + "; -fx-border-color: transparent;");

        Button btnAtualizar = new Button("Atualizar");
        estilizarBotaoConfirmar(btnAtualizar);
        btnAtualizar.setOnAction(e -> abrirModuloCozinha()); // recarrega

        Button btnVoltar = new Button("Voltar");
        estilizarBotaoCancelar(btnVoltar);
        btnVoltar.setOnAction(e -> mostrarMenuPrincipal());

        HBox barraInferior = new HBox(10, btnAtualizar, btnVoltar);
        barraInferior.setAlignment(Pos.CENTER_RIGHT);
        barraInferior.setPadding(new Insets(10, 20, 20, 20));

        BorderPane root = new BorderPane();
        root.setTop(topo);
        root.setCenter(scroll);
        root.setBottom(barraInferior);
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);
    }

    private HBox criarCardPedidoCozinha(Pedido pedido) {
        String tipo = pedido.getTipoAtendimento() == null ? "" : pedido.getTipoAtendimento();
        String titulo;

        if ("MESA".equalsIgnoreCase(tipo)) {
            titulo = "Mesa " + pedido.getIdMesa();
        } else {
            String obs = pedido.getObservacao();
            if (obs == null || obs.isBlank()) {
                titulo = "Retirada";
            } else {
                titulo = "Retirada - " + obs;
            }
        }

        String status = pedido.getStatus() == null ? "" : pedido.getStatus();

        Label lblMesa = new Label(titulo);
        lblMesa.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblMesa.setTextFill(Color.web(COR_CREME));

        Label lblInfo = new Label("Pedido #" + pedido.getIdPedido() + " - Status: " + status);
        lblInfo.setFont(Font.font("System", 13));
        lblInfo.setTextFill(Color.web(COR_CREME));

        VBox esquerda = new VBox(5, lblMesa, lblInfo);
        esquerda.setAlignment(Pos.CENTER_LEFT);

        Button btnDetalhes = new Button("Detalhes");
        estilizarBotaoConfirmar(btnDetalhes);
        btnDetalhes.setOnAction(e -> {
            String cabecalho = titulo;
            mostrarItensPedidoDetalhado(cabecalho, pedido.getIdPedido());
        });

        // Botão
        Button btnEmPreparo = new Button("Em preparação");
        estilizarBotaoConfirmar(btnEmPreparo);
        String statusUpper = status.toUpperCase();
        btnEmPreparo.setDisable(statusUpper.equals("EM PREPARAÇÃO") || statusUpper.equals("PRONTO"));
        btnEmPreparo.setOnAction(e -> {
            pedidoDAO.atualizarStatusPedido(pedido.getIdPedido(), "EM PREPARAÇÃO");
            mostrarMensagemTema("Status atualizado",
                    "O pedido #" + pedido.getIdPedido() + " foi marcado como EM PREPARAÇÃO.");
            abrirModuloCozinha();
        });

        Button btnPronto = new Button("Pedido pronto");
        estilizarBotaoConfirmar(btnPronto);
        btnPronto.setOnAction(e -> {
            pedidoDAO.atualizarStatusPedido(pedido.getIdPedido(), "PRONTO");
            mostrarMensagemTema("Pedido pronto",
                    "O pedido #" + pedido.getIdPedido() + " foi marcado como PRONTO.");
            abrirModuloCozinha();
        });

        HBox linhaBotoes = new HBox(10, btnDetalhes, btnEmPreparo, btnPronto);
        linhaBotoes.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, esquerda, linhaBotoes);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #23453B; -fx-background-radius: 12;");

        HBox card = new HBox(box);
        card.setPadding(new Insets(8, 8, 8, 8));
        return card;
    }


    // ITENS DO PEDIDO

    private void mostrarItensPedidoDetalhado(String cabecalho, int idPedido) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Itens do pedido");

        Label lblTitulo = new Label("Itens do pedido");
        lblTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web(COR_CREME));

        Label lblSub = new Label(cabecalho + " - Pedido #" + idPedido);
        lblSub.setFont(Font.font("System", 14));
        lblSub.setTextFill(Color.web(COR_CREME));

        TableView<ItemPedidoResumo> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabela.setPrefHeight(260);

        TableColumn<ItemPedidoResumo, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));

        TableColumn<ItemPedidoResumo, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        TableColumn<ItemPedidoResumo, Double> colValorUnit = new TableColumn<>("Valor unitário");
        colValorUnit.setCellValueFactory(new PropertyValueFactory<>("valorUnitario"));

        TableColumn<ItemPedidoResumo, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalItem"));

        tabela.getColumns().addAll(colProduto, colQtd, colValorUnit, colTotal);

        ObservableList<ItemPedidoResumo> dados = carregarItensPedidoDoBanco(idPedido);
        tabela.setItems(dados);

        double totalGeral = dados.stream().mapToDouble(ItemPedidoResumo::getTotalItem).sum();
        Label lblTotal = new Label(String.format("Total do pedido: R$ %.2f", totalGeral));
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTotal.setTextFill(Color.web(COR_CREME));

        Button btnOk = new Button("OK");
        estilizarBotaoConfirmar(btnOk);
        btnOk.setOnAction(e -> dialog.close());

        HBox linhaTotal = new HBox(lblTotal);
        linhaTotal.setAlignment(Pos.CENTER_LEFT);

        HBox linhaBotao = new HBox(btnOk);
        linhaBotao.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, lblTitulo, lblSub, tabela, linhaTotal, linhaBotao);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:" + COR_FUNDO + "; -fx-background-radius: 14;");

        Scene scene = new Scene(root, 600, 420);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Busca os itens do pedido diretamente no banco (tabela item_pedido + produto).
     */
    private ObservableList<ItemPedidoResumo> carregarItensPedidoDoBanco(int idPedido) {
        ObservableList<ItemPedidoResumo> itens = FXCollections.observableArrayList();

        String sql =
                "SELECT ip.quantidade, ip.valor_unitario, p.nome " +
                        "FROM item_pedido ip " +
                        "JOIN produto p ON ip.id_produto = p.id_produto " +
                        "WHERE ip.id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    int qtd = rs.getInt("quantidade");
                    double valorUnit = rs.getDouble("valor_unitario");
                    double totalItem = valorUnit * qtd;

                    itens.add(new ItemPedidoResumo(nome, qtd, valorUnit, totalItem));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    /**
     * Classe simples usada para exibir as linhas na TableView de itens do pedido.
     */
    public static class ItemPedidoResumo {
        private final StringProperty nomeProduto = new SimpleStringProperty();
        private final IntegerProperty quantidade = new SimpleIntegerProperty();
        private final DoubleProperty valorUnitario = new SimpleDoubleProperty();
        private final DoubleProperty totalItem = new SimpleDoubleProperty();

        public ItemPedidoResumo(String nomeProduto, int quantidade, double valorUnitario, double totalItem) {
            this.nomeProduto.set(nomeProduto);
            this.quantidade.set(quantidade);
            this.valorUnitario.set(valorUnitario);
            this.totalItem.set(totalItem);
        }

        public String getNomeProduto() {
            return nomeProduto.get();
        }

        public int getQuantidade() {
            return quantidade.get();
        }

        public double getValorUnitario() {
            return valorUnitario.get();
        }

        public double getTotalItem() {
            return totalItem.get();
        }
    }


    // MAIN

    public static void main(String[] args) {
        launch(args);
    }
}
