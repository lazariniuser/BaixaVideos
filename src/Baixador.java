import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Baixador extends JFrame {

        // =========================================================
        // CORES
        // =========================================================

        private static final Color BG = new Color(18, 18, 22);
        private static final Color PANEL = new Color(27, 27, 33);
        private static final Color INPUT = new Color(35, 35, 42);
        private static final Color TEXT = new Color(235, 235, 240);
        private static final Color SECONDARY = new Color(160, 160, 170);
        private static final Color ACCENT = new Color(90, 130, 255);
        private static final Color ACCENT_HOVER = new Color(110, 145, 255);
        private static final Color SUCCESS = new Color(80, 200, 120);
        private static final Color ERROR = new Color(230, 80, 80);

        // =========================================================
        // COMPONENTES
        // =========================================================

        private JTextField urlField;
        private JTextField folderField;

        private JComboBox<String> qualityCombo;

        private JButton downloadButton;
        private JButton cancelButton;
        private JButton folderButton;

        private JProgressBar progressBar;

        private JLabel statusLabel;
        private JLabel titleLabel;
        private JLabel speedLabel;
        private JLabel etaLabel;
        private JLabel sizeLabel;

        private JTextArea logArea;

        // Processo do yt-dlp
        private Process currentProcess;

        // =========================================================
        // CONSTRUTOR
        // =========================================================

        public Baixador() {

                setTitle("Baixador do Youtube");
                setSize(760, 650);
                setMinimumSize(new Dimension(680, 580));

                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);

                criarInterface();

                verificarFerramentas();
        }

        // =========================================================
        // INTERFACE
        // =========================================================

        private void criarInterface() {

                JPanel background = new JPanel(new BorderLayout());
                background.setBackground(BG);

                background.setBorder(new EmptyBorder(25, 30, 25, 30));

                // -----------------------------------------------------
                // CABEÇALHO
                // -----------------------------------------------------

                JPanel header = new JPanel();
                header.setOpaque(false);
                header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

                JLabel title = new JLabel("YouTube Downloader");

                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 28));

                JLabel subtitle = new JLabel(
                                "Baixe vídeos na melhor qualidade disponível");

                subtitle.setForeground(SECONDARY);
                subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                header.add(title);
                header.add(Box.createVerticalStrut(5));
                header.add(subtitle);

                background.add(header, BorderLayout.NORTH);

                // -----------------------------------------------------
                // PAINEL CENTRAL
                // -----------------------------------------------------

                JPanel center = new JPanel();
                center.setOpaque(false);

                center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

                // URL

                center.add(criarLabel("URL do vídeo"));

                JPanel urlPanel = new JPanel(new BorderLayout(10, 0));
                urlPanel.setOpaque(false);
                urlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

                urlField = criarTextField();

                JButton pasteButton = criarButton("Colar");

                pasteButton.addActionListener(e -> colarURL());

                urlPanel.add(urlField, BorderLayout.CENTER);
                urlPanel.add(pasteButton, BorderLayout.EAST);

                center.add(urlPanel);

                center.add(Box.createVerticalStrut(18));

                // QUALIDADE

                center.add(criarLabel("Qualidade"));

                qualityCombo = new JComboBox<>();

                qualityCombo.addItem("Melhor qualidade disponível");
                qualityCombo.addItem("2160p / 4K");
                qualityCombo.addItem("1440p");
                qualityCombo.addItem("1080p");
                qualityCombo.addItem("720p");
                qualityCombo.addItem("480p");

                estilizarCombo(qualityCombo);

                qualityCombo.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 42));

                center.add(qualityCombo);

                center.add(Box.createVerticalStrut(18));

                // PASTA

                center.add(criarLabel("Pasta de destino"));

                JPanel folderPanel = new JPanel(new BorderLayout(10, 0));
                folderPanel.setOpaque(false);

                folderField = criarTextField();

                folderField.setText(
                                System.getProperty("user.home")
                                                + File.separator
                                                + "Downloads");

                folderButton = criarButton("Selecionar");

                folderButton.addActionListener(e -> selecionarPasta());

                folderPanel.add(folderField, BorderLayout.CENTER);
                folderPanel.add(folderButton, BorderLayout.EAST);

                folderPanel.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 45));

                center.add(folderPanel);

                center.add(Box.createVerticalStrut(25));

                // INFORMAÇÕES

                JPanel infoPanel = new JPanel(new GridLayout(2, 2, 15, 8));

                infoPanel.setBackground(PANEL);
                infoPanel.setBorder(
                                new EmptyBorder(15, 18, 15, 18));

                titleLabel = criarInfoLabel("Nenhum vídeo selecionado");
                speedLabel = criarInfoLabel("Velocidade: --");
                etaLabel = criarInfoLabel("Tempo restante: --");
                sizeLabel = criarInfoLabel("Tamanho: --");

                infoPanel.add(titleLabel);
                infoPanel.add(speedLabel);
                infoPanel.add(etaLabel);
                infoPanel.add(sizeLabel);

                center.add(infoPanel);

                center.add(Box.createVerticalStrut(20));

                // PROGRESSO

                progressBar = new JProgressBar(0, 100);

                progressBar.setValue(0);
                progressBar.setStringPainted(true);

                progressBar.setForeground(ACCENT);
                progressBar.setBackground(INPUT);

                progressBar.setBorderPainted(false);

                progressBar.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 25));

                center.add(progressBar);

                center.add(Box.createVerticalStrut(8));

                statusLabel = new JLabel("Pronto para baixar");

                statusLabel.setForeground(SECONDARY);
                statusLabel.setFont(
                                new Font("Segoe UI", Font.PLAIN, 13));

                center.add(statusLabel);

                center.add(Box.createVerticalStrut(15));

                // BOTÕES

                JPanel buttonPanel = new JPanel(
                                new GridLayout(1, 2, 10, 0));

                buttonPanel.setOpaque(false);

                downloadButton = criarButton("BAIXAR VÍDEO");

                cancelButton = criarButton("CANCELAR");

                cancelButton.setEnabled(false);

                downloadButton.addActionListener(
                                e -> iniciarDownload());

                cancelButton.addActionListener(
                                e -> cancelarDownload());

                buttonPanel.add(downloadButton);
                buttonPanel.add(cancelButton);

                center.add(buttonPanel);

                center.add(Box.createVerticalStrut(18));

                // LOG

                JLabel logTitle = criarLabel("Log");

                center.add(logTitle);

                logArea = new JTextArea();

                logArea.setEditable(false);
                logArea.setLineWrap(true);
                logArea.setWrapStyleWord(true);

                logArea.setForeground(SECONDARY);
                logArea.setBackground(PANEL);

                logArea.setFont(
                                new Font("Consolas", Font.PLAIN, 12));

                JScrollPane scroll = new JScrollPane(logArea);

                scroll.setBorder(null);

                scroll.setPreferredSize(
                                new Dimension(0, 130));

                center.add(scroll);

                background.add(center, BorderLayout.CENTER);

                setContentPane(background);
        }

        // =========================================================
        // CRIAÇÃO DE COMPONENTES
        // =========================================================

        private JLabel criarLabel(String texto) {

                JLabel label = new JLabel(texto);

                label.setForeground(TEXT);

                label.setFont(
                                new Font("Segoe UI", Font.BOLD, 13));

                label.setAlignmentX(Component.LEFT_ALIGNMENT);

                return label;
        }

        private JLabel criarInfoLabel(String texto) {

                JLabel label = new JLabel(texto);

                label.setForeground(TEXT);

                label.setFont(
                                new Font("Segoe UI", Font.PLAIN, 12));

                return label;
        }

        private JTextField criarTextField() {

                JTextField field = new JTextField();

                field.setForeground(TEXT);
                field.setBackground(INPUT);

                field.setCaretColor(TEXT);

                field.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(55, 55, 65)),
                                                new EmptyBorder(8, 12, 8, 12)));

                field.setFont(
                                new Font("Segoe UI", Font.PLAIN, 14));

                return field;
        }

        private JButton criarButton(String texto) {

                JButton button = new JButton(texto);

                button.setForeground(Color.WHITE);
                button.setBackground(ACCENT);

                button.setFont(
                                new Font("Segoe UI", Font.BOLD, 13));

                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                button.addMouseListener(
                                new java.awt.event.MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        java.awt.event.MouseEvent e) {
                                                if (button.isEnabled()) {
                                                        button.setBackground(
                                                                        ACCENT_HOVER);
                                                }
                                        }

                                        @Override
                                        public void mouseExited(
                                                        java.awt.event.MouseEvent e) {
                                                button.setBackground(ACCENT);
                                        }
                                });

                return button;
        }

        private void estilizarCombo(
                        JComboBox<String> combo) {

                combo.setForeground(TEXT);
                combo.setBackground(INPUT);

                combo.setFont(
                                new Font("Segoe UI", Font.PLAIN, 14));

                combo.setBorder(
                                BorderFactory.createLineBorder(
                                                new Color(55, 55, 65)));
        }

        // =========================================================
        // VERIFICAR FERRAMENTAS
        // =========================================================

        private void verificarFerramentas() {

                Path ytDlp = Paths.get(
                                "tools",
                                "yt-dlp.exe");

                Path ffmpeg = Paths.get(
                                "tools",
                                "ffmpeg.exe");

                if (!Files.exists(ytDlp)) {

                        adicionarLog(
                                        "ERRO: tools/yt-dlp.exe não encontrado.");

                        statusLabel.setText(
                                        "yt-dlp.exe não encontrado");

                        statusLabel.setForeground(ERROR);

                } else {

                        adicionarLog(
                                        "yt-dlp encontrado.");
                }

                if (!Files.exists(ffmpeg)) {

                        adicionarLog(
                                        "AVISO: ffmpeg.exe não encontrado.");

                        adicionarLog(
                                        "Streams separados podem não ser combinados.");

                } else {

                        adicionarLog(
                                        "FFmpeg encontrado.");
                }
        }

        // =========================================================
        // COLAR URL
        // =========================================================

        private void colarURL() {

                try {

                        if (Toolkit.getDefaultToolkit()
                                        .getSystemClipboard()
                                        .isDataFlavorAvailable(
                                                        DataFlavor.stringFlavor)) {

                                String texto = (String) Toolkit
                                                .getDefaultToolkit()
                                                .getSystemClipboard()
                                                .getData(
                                                                DataFlavor.stringFlavor);

                                urlField.setText(texto.trim());
                        }

                } catch (Exception e) {

                        mostrarErro(
                                        "Não foi possível acessar a área de transferência.");
                }
        }

        // =========================================================
        // SELECIONAR PASTA
        // =========================================================

        private void selecionarPasta() {

                JFileChooser chooser = new JFileChooser();

                chooser.setDialogTitle(
                                "Selecione a pasta de destino");

                chooser.setFileSelectionMode(
                                JFileChooser.DIRECTORIES_ONLY);

                int resultado = chooser.showOpenDialog(this);

                if (resultado == JFileChooser.APPROVE_OPTION) {

                        folderField.setText(
                                        chooser.getSelectedFile()
                                                        .getAbsolutePath());
                }
        }

        // =========================================================
        // INICIAR DOWNLOAD
        // =========================================================

        private void iniciarDownload() {

                String url = urlField.getText().trim();

                String pasta = folderField.getText().trim();

                if (url.isEmpty()) {

                        mostrarErro(
                                        "Digite a URL do vídeo.");

                        return;
                }

                if (!url.startsWith("http://")
                                && !url.startsWith("https://")) {

                        mostrarErro(
                                        "A URL informada não parece válida.");

                        return;
                }

                if (pasta.isEmpty()) {

                        mostrarErro(
                                        "Selecione uma pasta de destino.");

                        return;
                }

                Path ytDlp = Paths.get(
                                "tools",
                                "yt-dlp.exe");

                if (!Files.exists(ytDlp)) {

                        mostrarErro(
                                        "yt-dlp.exe não foi encontrado em tools/.");

                        return;
                }

                try {

                        Files.createDirectories(
                                        Paths.get(pasta));

                } catch (IOException e) {

                        mostrarErro(
                                        "Não foi possível acessar a pasta.");

                        return;
                }

                downloadButton.setEnabled(false);
                cancelButton.setEnabled(true);
                folderButton.setEnabled(false);

                progressBar.setValue(0);

                statusLabel.setText(
                                "Obtendo informações do vídeo...");

                statusLabel.setForeground(SECONDARY);

                titleLabel.setText(
                                "Carregando...");

                speedLabel.setText(
                                "Velocidade: --");

                etaLabel.setText(
                                "Tempo restante: --");

                sizeLabel.setText(
                                "Tamanho: --");

                logArea.setText("");

                adicionarLog(
                                "Iniciando download...");

                new Thread(
                                () -> executarDownload(url, pasta),
                                "DownloadThread").start();
        }

        // =========================================================
        // EXECUTAR YT-DLP
        // =========================================================

        private void executarDownload(
                        String url,
                        String pasta) {

                try {

                        List<String> comando = new ArrayList<>();

                        comando.add(
                                        Paths.get(
                                                        "tools",
                                                        "yt-dlp.exe").toString());

                        // -------------------------------------------------
                        // MELHOR QUALIDADE
                        // -------------------------------------------------

                        String qualidade = (String) qualityCombo
                                        .getSelectedItem();

                        if (qualidade == null
                                        || qualidade.equals(
                                                        "Melhor qualidade disponível")) {

                                comando.add("-f");

                                comando.add(
                                                "bv*+ba/b");

                        } else {

                                String limite = obterLimiteQualidade(
                                                qualidade);

                                comando.add("-f");

                                comando.add(
                                                "bv*[height<="
                                                                + limite
                                                                + "]+ba/b[height<="
                                                                + limite
                                                                + "]");
                        }

                        // -------------------------------------------------
                        // MP4
                        // -------------------------------------------------

                        comando.add(
                                        "--merge-output-format");

                        comando.add("mp4");

                        // -------------------------------------------------
                        // PROGRESSO
                        // -------------------------------------------------

                        comando.add(
                                        "--newline");

                        comando.add(
                                        "--progress");

                        comando.add(
                                        "--progress-template");

                        comando.add(
                                        "download:%(progress._percent_str)s|%(progress._speed_str)s|%(progress._eta)s|%(progress.downloaded_bytes)s|%(progress.total_bytes)s|%(info.title)s");

                        // -------------------------------------------------
                        // PASTA
                        // -------------------------------------------------

                        comando.add("-P");

                        comando.add(pasta);

                        // -------------------------------------------------
                        // NOME DO ARQUIVO
                        // -------------------------------------------------

                        comando.add("-o");

                        comando.add(
                                        "%(title)s [%(id)s].%(ext)s");

                        // -------------------------------------------------
                        // URL
                        // -------------------------------------------------

                        comando.add(url);

                        adicionarLog(
                                        "Executando yt-dlp...");

                        ProcessBuilder builder = new ProcessBuilder(comando);

                        builder.redirectErrorStream(true);

                        currentProcess = builder.start();

                        BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(
                                                        currentProcess
                                                                        .getInputStream(),
                                                        StandardCharsets.UTF_8));

                        String linha;

                        while ((linha = reader.readLine()) != null) {

                                processarLinha(linha);
                        }

                        int exitCode = currentProcess.waitFor();

                        currentProcess = null;

                        if (exitCode == 0) {

                                SwingUtilities.invokeLater(() -> {

                                        progressBar.setValue(100);

                                        statusLabel.setText(
                                                        "Download concluído!");

                                        statusLabel.setForeground(
                                                        SUCCESS);

                                        adicionarLog(
                                                        "Download concluído com sucesso.");

                                        downloadButton.setEnabled(true);
                                        cancelButton.setEnabled(false);
                                        folderButton.setEnabled(true);

                                });

                        } else {

                                SwingUtilities.invokeLater(() -> {

                                        statusLabel.setText(
                                                        "Erro durante o download.");

                                        statusLabel.setForeground(ERROR);

                                        adicionarLog(
                                                        "yt-dlp terminou com código: "
                                                                        + exitCode);

                                        downloadButton.setEnabled(true);
                                        cancelButton.setEnabled(false);
                                        folderButton.setEnabled(true);
                                });
                        }

                } catch (Exception e) {

                        currentProcess = null;

                        SwingUtilities.invokeLater(() -> {

                                statusLabel.setText(
                                                "Erro.");

                                statusLabel.setForeground(ERROR);

                                adicionarLog(
                                                "Erro: "
                                                                + e.getMessage());

                                downloadButton.setEnabled(true);
                                cancelButton.setEnabled(false);
                                folderButton.setEnabled(true);
                        });
                }
        }

        // =========================================================
        // PROCESSAR PROGRESSO
        // =========================================================

        private void processarLinha(
                        String linha) {

                if (linha == null
                                || linha.isBlank()) {
                        return;
                }

                adicionarLog(linha);

                // -----------------------------------------------------
                // FORMATO:
                //
                // 35.2%|4.2MiB/s|00:42|...
                // -----------------------------------------------------

                if (linha.startsWith("download:")) {

                        String dados = linha.substring(
                                        "download:".length());

                        String[] partes = dados.split(
                                        "\\|",
                                        -1);

                        if (partes.length >= 6) {

                                String porcentagem = partes[0].trim();

                                String velocidade = partes[1].trim();

                                String eta = partes[2].trim();

                                String baixado = partes[3].trim();

                                String total = partes[4].trim();

                                String titulo = partes[5].trim();

                                atualizarInterface(
                                                porcentagem,
                                                velocidade,
                                                eta,
                                                baixado,
                                                total,
                                                titulo);
                        }
                }
        }

        // =========================================================
        // ATUALIZAR INTERFACE
        // =========================================================

        private void atualizarInterface(
                        String porcentagem,
                        String velocidade,
                        String eta,
                        String baixado,
                        String total,
                        String titulo) {

                SwingUtilities.invokeLater(() -> {

                        try {

                                String numero = porcentagem
                                                .replace("%", "")
                                                .replace(",", ".")
                                                .trim();

                                double valor = Double.parseDouble(numero);

                                progressBar.setValue(
                                                (int) valor);

                        } catch (Exception ignored) {
                        }

                        if (!titulo.isBlank()) {

                                titleLabel.setText(
                                                titulo);
                        }

                        speedLabel.setText(
                                        "Velocidade: "
                                                        + velocidade);

                        etaLabel.setText(
                                        "Tempo restante: "
                                                        + eta);

                        sizeLabel.setText(
                                        "Tamanho: "
                                                        + formatarBytes(baixado)
                                                        + " / "
                                                        + formatarBytes(total));

                        statusLabel.setText(
                                        "Baixando...");
                });
        }

        // =========================================================
        // FORMATAR BYTES
        // =========================================================

        private String formatarBytes(
                        String valor) {

                try {

                        long bytes = Long.parseLong(
                                        valor);

                        if (bytes < 1024) {
                                return bytes + " B";
                        }

                        double kb = bytes / 1024.0;

                        if (kb < 1024) {

                                return String.format(
                                                "%.1f KB",
                                                kb);
                        }

                        double mb = kb / 1024.0;

                        if (mb < 1024) {

                                return String.format(
                                                "%.1f MB",
                                                mb);
                        }

                        double gb = mb / 1024.0;

                        return String.format(
                                        "%.2f GB",
                                        gb);

                } catch (Exception e) {

                        return valor;
                }
        }

        // =========================================================
        // QUALIDADE
        // =========================================================

        private String obterLimiteQualidade(
                        String qualidade) {

                switch (qualidade) {

                        case "2160p / 4K":
                                return "2160";

                        case "1440p":
                                return "1440";

                        case "1080p":
                                return "1080";

                        case "720p":
                                return "720";

                        case "480p":
                                return "480";

                        default:
                                return "2160";
                }
        }

        // =========================================================
        // CANCELAR
        // =========================================================

        private void cancelarDownload() {

                if (currentProcess != null) {

                        adicionarLog(
                                        "Cancelando download...");

                        currentProcess.destroy();

                        currentProcess = null;

                        progressBar.setValue(0);

                        statusLabel.setText(
                                        "Download cancelado.");

                        statusLabel.setForeground(
                                        SECONDARY);

                        downloadButton.setEnabled(true);
                        cancelButton.setEnabled(false);
                        folderButton.setEnabled(true);
                }
        }

        // =========================================================
        // LOG
        // =========================================================

        private void adicionarLog(
                        String texto) {

                SwingUtilities.invokeLater(() -> {

                        logArea.append(
                                        texto + "\n");

                        logArea.setCaretPosition(
                                        logArea.getDocument()
                                                        .getLength());
                });
        }

        // =========================================================
        // ERRO
        // =========================================================

        private void mostrarErro(
                        String mensagem) {

                JOptionPane.showMessageDialog(
                                this,
                                mensagem,
                                "Erro",
                                JOptionPane.ERROR_MESSAGE);
        }

        // =========================================================
        // MAIN
        // =========================================================

        public static void main(
                        String[] args) {

                try {

                        UIManager.setLookAndFeel(
                                        UIManager
                                                        .getSystemLookAndFeelClassName());

                } catch (Exception ignored) {
                }

                SwingUtilities.invokeLater(() -> {

                        Baixador app = new Baixador();

                        app.setVisible(true);
                });
        }
}
