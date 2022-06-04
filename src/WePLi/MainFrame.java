/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package WePLi;

import Controller.PlayBsideTrackController;
import Controller.PlaylistController;
import Controller.RelaylistController;
import Controller.SongController;
import Dto.PlayBsideTrack.PlayBsideTrackDto;
import Dto.Playlist.PlaylistCreateDto;
import Dto.Playlist.PlaylistDto;
import Dto.Relaylist.RelaylistCreateDto;
import Dto.Relaylist.RelaylistDto;
import Dto.Song.SongCreateDto;
import Dto.Song.SongDto;
import Entity.SongChart.SongChart;
import WePLi.SearchFrame.SearchFrame;
import WePLi.UI.ComponentSetting;
import static WePLi.UI.ComponentSetting.convertSongToHtml;
import WePLi.UI.DataParser;
import WePLi.UI.JFrameSetting;
import WePLi.UI.JPanelSetting;
import WePLi.UI.JTableSetting;
import java.awt.Color;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static WePLi.UI.ComponentSetting.convertListToHtml;

/**
 *
 * @author Donghyeon <20183188>
 */
public class MainFrame extends javax.swing.JFrame {
    /**
     * Creates new form MainFrame
     */
    
    private ArrayList<JPanel> panelList = new ArrayList<>();
    
    private SongController songController = SongController.getInstance();// 컨트롤러 생성
    private PlaylistController playlistController = PlaylistController.getInstance();
    private PlayBsideTrackController playBsideTrackController = PlayBsideTrackController.getInstance();
    private RelaylistController relaylistController = RelaylistController.getInstance();
    
    public MainFrame() {
        JFrameSetting.layoutInit();

        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);

        JPanelSetting.changePanel(panelList, createPlayPanel);

        /* ChartTable 기본 디자인 세팅 (Customize Code에 있음) */
        /* PlaylistTable 기본 디자인 세팅 (Customize Code에 있음) */
        
        /* 테스트 값 생성 */
        String url1 = "https://image.genie.co.kr/Y/IMAGE/IMG_ALBUM/082/662/688/82662688_1651196114166_1_600x600.JPG/dims/resize/Q_80,0";
        String url2 = "https://image.bugsm.co.kr/album/images/50/40756/4075667.jpg?version=20220515063240.0";


        String genieUrl = "https://image.genie.co.kr/Y/IMAGE/IMG_ALBUM/082/540/759/82540759_1645152997958_1_600x600.JPG/dims/resize/Q_80,0";
        String bugsUrl = "https://image.bugsm.co.kr/album/images/912/40757/4075727.jpg?version=20220518025622.0";

        relayImageLabel.setIcon(ComponentSetting.getBigBlurImage("https://cdnimg.melon.co.kr/cm2/album/images/109/03/868/10903868_20220330103544_500.jpg?e89c53bde5d39b21b09e8007db5b9cc0/melon/resize/912/quality/80/optimize"));
    }
    
    /*------------------------------- 릴레이리스트 관련 메소드 --------------------------------*/
    // 릴레이리스트 생성 메소드
    private void createRelaylist(){
        TableModel tm = this.relayFirstTable.getModel();
        int row = tm.getRowCount();
        
        if(row == 0) return;    // 한 곡도 선택하지 않은 경우
        
        // 제목, 가수, 이미지, 앨범 필요
        // 선택한 곡을 Songlist로 변환
        ArrayList<SongCreateDto> songlist = new ArrayList<>();        
        SongCreateDto firstSong = DataParser.parseHtmlToSong(tm.getValueAt(0, 2));  
        songlist.add(firstSong); // 리스트 타입으로 전달 (addSongList 형식 맞추기 위해서)
        
        // 1. 선택한 곡 Song 테이블에 저장
        ArrayList<SongDto> songDtolist = songController.addSongList(songlist);
        
        // 2. 릴레이리스트 저장
        RelaylistCreateDto relaylistCreateDto = RelaylistCreateDto.builder()
                                               .title(createRelayTitleField.getText())
                                               .inform(createRelayInformTextArea.getText())
                                               .author(LoginUserLabel.getText())
                                               .firstSongTitle(firstSong.getTitle())
                                               .firstSongSinger(firstSong.getSinger())
                                               .firstSongAlbum(firstSong.getAlbum())
                                               .firstSongImage(firstSong.getImage())
                                               .createTime(new java.sql.Date(new java.util.Date().getTime()))
                                               .build();
        
        
        RelaylistDto relaylistDto = relaylistController.createRelaylist(relaylistCreateDto);
        
        if(!Objects.isNull(relaylistDto)) JOptionPane.showMessageDialog(null, "릴레이리스트 생성이 완료 되었습니다.");
        else JOptionPane.showMessageDialog(null, "릴레이리스트 생성에 실패 했습니다.");
    }
    
    // 릴레이리스트 목록 출력(조회) 메소드
    private void getAllRelaylists(){
        JPanelSetting.changePanel(this.panelList, this.relaylistPanel);        

        // 릴레이리스트 테이블 초기화
        DefaultTableModel model = (DefaultTableModel) relaylistTable.getModel();
        model.setRowCount(0);
        
        // 1. 모든 릴레이리스트 가져오기
        ArrayList<RelaylistDto> relaylists = relaylistController.getRelaylists();
        Object[][] data = new Object[relaylists.size()][];
        
        // 2. 릴레이리스트 정보 추출
        for (int i = 0; i < relaylists.size(); i++) {
            String relaylistId = relaylists.get(i).getId();
            String title = relaylists.get(i).getTitle();
            String author = relaylists.get(i).getAuthor();
            String inform = relaylists.get(i).getInform();
            String imageUrl = relaylists.get(i).getFirstSongImage();
            ImageIcon image = ComponentSetting.imageToIcon(imageUrl, 100, 100);
            java.sql.Date createTime = relaylists.get(i).getCreateTime();
            
            data[i] = new Object[]{
                model.getRowCount() + (i + 1),
                image,
                convertListToHtml(relaylistId, title, author, inform),
                createTime
            };
        }
                
        // 테이블에 릴레이리스트 삽입
        JTableSetting.insertTableRow((DefaultTableModel) relaylistTable.getModel(), data);
    }
    /*----------------------------- 릴레이리스트 관련 메소드 끝 --------------------------------*/
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BackgroundPanel = new javax.swing.JPanel();
        LoginUserLabel = new javax.swing.JLabel();
        HomeLabel = new javax.swing.JLabel();
        PlaylistLabel = new javax.swing.JLabel();
        RelaylistLabel = new javax.swing.JLabel();
        NotifyLabel = new javax.swing.JLabel();
        HeaderLabel = new javax.swing.JLabel();
        SidebarLabel = new javax.swing.JLabel();
        relaylistPanel = new javax.swing.JPanel();
        addRelaylistBtn = new javax.swing.JButton();
        relaylistScrollPanel = new javax.swing.JScrollPane();
        relaylistTable = new javax.swing.JTable();
        playlistPanel = new javax.swing.JPanel();
        addPlaylistBtn = new javax.swing.JButton();
        playlistScrollPanel = new javax.swing.JScrollPane();
        playlistTable = new javax.swing.JTable();
        createRelayPanel = new javax.swing.JPanel();
        relayInformScrollPanel = new javax.swing.JScrollPane();
        createRelayInformTextArea = new javax.swing.JTextArea();
        createRelayTitleField = new javax.swing.JTextField();
        relayFirstScrollPanel = new javax.swing.JScrollPane();
        relayFirstTable = new javax.swing.JTable();
        addSongBtn = new javax.swing.JButton();
        createRelayBtn = new javax.swing.JButton();
        createRelayInformLabel = new javax.swing.JLabel();
        createRelayTitleLabel = new javax.swing.JLabel();
        createRelayImgLabel = new javax.swing.JLabel();
        createRelayBGLabel = new javax.swing.JLabel();
        createPlayPanel = new javax.swing.JPanel();
        playInformScrollPanel = new javax.swing.JScrollPane();
        createPlayInformTextArea = new javax.swing.JTextArea();
        playBsideScrollPanel = new javax.swing.JScrollPane();
        playBsideTable = new javax.swing.JTable();
        createPlayTitleField = new javax.swing.JTextField();
        createPlayBtn = new javax.swing.JButton();
        addTrackBtn = new javax.swing.JButton();
        createPlayImgLabel = new javax.swing.JLabel();
        createPlayTitleLabel = new javax.swing.JLabel();
        createPlayInformLabel = new javax.swing.JLabel();
        createPlayBGLabel = new javax.swing.JLabel();
        relaylistDetailPanel = new javax.swing.JPanel();
        relaylistInformLabel = new javax.swing.JLabel();
        firstSongImageLabel = new javax.swing.JLabel();
        firstSongTitleLabel = new javax.swing.JLabel();
        relaylistTitleLabel = new javax.swing.JLabel();
        firstSongSingerLabel = new javax.swing.JLabel();
        blurLabel = new javax.swing.JLabel();
        relayImageLabel = new javax.swing.JLabel();
        relayDetailScrollPanel = new javax.swing.JScrollPane();
        relayDetailTable = new javax.swing.JTable();
        playlistDetailPanel = new javax.swing.JPanel();
        playDeleteBtn = new javax.swing.JButton();
        playEditBtn = new javax.swing.JButton();
        playImageLabel = new javax.swing.JLabel();
        playTitleLabel = new javax.swing.JLabel();
        playInformLabel = new javax.swing.JLabel();
        playDateLabel = new javax.swing.JLabel();
        playAuthorLabel = new javax.swing.JLabel();
        playIdLabel = new javax.swing.JLabel();
        playDetailScrollPanel = new javax.swing.JScrollPane();
        playDetailTable = new javax.swing.JTable();
        chartPanel = new javax.swing.JPanel();
        chartScrollPanel = new javax.swing.JScrollPane();
        chartTable = new javax.swing.JTable();
        notifyPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        BackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));
        BackgroundPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        LoginUserLabel.setFont(new java.awt.Font("나눔스퀘어 Bold", 0, 18)); // NOI18N
        LoginUserLabel.setText("admin");
        BackgroundPanel.add(LoginUserLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 670, 70, 40));

        HomeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/menu/normal/home.png"))); // NOI18N
        HomeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                HomeLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HomeLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HomeLabelMouseExited(evt);
            }
        });
        BackgroundPanel.add(HomeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 168, 55));

        PlaylistLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/menu/normal/playlist.png"))); // NOI18N
        PlaylistLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PlaylistLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                PlaylistLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                PlaylistLabelMouseExited(evt);
            }
        });
        BackgroundPanel.add(PlaylistLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 115, 168, 55));

        RelaylistLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/menu/normal/relaylist.png"))); // NOI18N
        RelaylistLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RelaylistLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                RelaylistLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                RelaylistLabelMouseExited(evt);
            }
        });
        BackgroundPanel.add(RelaylistLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 168, 55));

        NotifyLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/menu/normal/notify.png"))); // NOI18N
        NotifyLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NotifyLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NotifyLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NotifyLabelMouseExited(evt);
            }
        });
        BackgroundPanel.add(NotifyLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 225, 168, 55));

        HeaderLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/header.png"))); // NOI18N
        BackgroundPanel.add(HeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 74));

        SidebarLabel.setBackground(new java.awt.Color(39, 49, 64));
        SidebarLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/sidebar.png"))); // NOI18N
        BackgroundPanel.add(SidebarLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 168, -1));

        relaylistPanel.setBackground(new java.awt.Color(255, 255, 255));
        relaylistPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addRelaylistBtn.setBackground(new java.awt.Color(255,255,255,0));
        addRelaylistBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/addTrack_btn.png"))); // NOI18N
        addRelaylistBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addRelaylistBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addRelaylistBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addRelaylistBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addRelaylistBtnMouseExited(evt);
            }
        });
        relaylistPanel.add(addRelaylistBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 120, 57, 22));

        relaylistScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        relaylistScrollPanel.setBorder(null);
        relaylistScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        relaylistScrollPanel.setToolTipText("");
        relaylistScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        relaylistTable.setBackground(new java.awt.Color(255,255,255,0));
        relaylistTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        relaylistTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "공백", "사진", "플레이리스트                                              ", "날짜"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        relaylistTable.setMinimumSize(new java.awt.Dimension(10, 400));
        relaylistTable.setOpaque(false);
        relaylistTable.setRowHeight(100);
        relaylistTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        relaylistTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        relaylistTable.getTableHeader().setResizingAllowed(false);
        relaylistTable.getTableHeader().setReorderingAllowed(false);
        relaylistTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                relaylistTableMouseClicked(evt);
            }
        });
        relaylistScrollPanel.setViewportView(relaylistTable);
        /* PlaylistTable 기본 세팅 */
        JTableSetting.tableInit(relaylistScrollPanel, relaylistTable);
        JTableSetting.tableHeaderInit(relaylistTable, relaylistPanel.getWidth(), 40);
        JTableSetting.listTableSetting(relaylistTable);

        relaylistPanel.add(relaylistScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 148, 896, 510));

        BackgroundPanel.add(relaylistPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, 910, 660));
        panelList.add(relaylistPanel);

        playlistPanel.setBackground(new java.awt.Color(255, 255, 255));
        playlistPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addPlaylistBtn.setBackground(new java.awt.Color(255,255,255,0));
        addPlaylistBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/addTrack_btn.png"))); // NOI18N
        addPlaylistBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addPlaylistBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addPlaylistBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addPlaylistBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addPlaylistBtnMouseExited(evt);
            }
        });
        playlistPanel.add(addPlaylistBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 120, 57, 22));

        playlistScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        playlistScrollPanel.setBorder(null);
        playlistScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        playlistScrollPanel.setToolTipText("");
        playlistScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        playlistTable.setBackground(new java.awt.Color(255,255,255,0));
        playlistTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        playlistTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "공백", "사진", "플레이리스트                                              ", "날짜"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        playlistTable.setMinimumSize(new java.awt.Dimension(10, 400));
        playlistTable.setOpaque(false);
        playlistTable.setRowHeight(100);
        playlistTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        playlistTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        playlistTable.getTableHeader().setResizingAllowed(false);
        playlistTable.getTableHeader().setReorderingAllowed(false);
        playlistTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playlistTableMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                playlistTableMousePressed(evt);
            }
        });
        playlistScrollPanel.setViewportView(playlistTable);
        /* PlaylistTable 기본 세팅 */
        JTableSetting.tableInit(playlistScrollPanel, playlistTable);
        JTableSetting.tableHeaderInit(playlistTable, playlistPanel.getWidth(), 40);
        JTableSetting.listTableSetting(playlistTable);

        playlistPanel.add(playlistScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 154, 896, 500));

        BackgroundPanel.add(playlistPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, 910, 660));
        panelList.add(playlistPanel);

        createRelayPanel.setBackground(new java.awt.Color(255, 255, 255));
        createRelayPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        relayInformScrollPanel.setBackground(new java.awt.Color(255, 255, 255));
        relayInformScrollPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        createRelayInformTextArea.setBackground(new java.awt.Color(255, 255, 255));
        createRelayInformTextArea.setColumns(20);
        createRelayInformTextArea.setFont(new java.awt.Font("나눔스퀘어", 0, 12)); // NOI18N
        createRelayInformTextArea.setRows(5);
        createRelayInformTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createRelayInformTextArea.setOpaque(false);
        createRelayInformTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                createRelayInformTextAreaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                createRelayInformTextAreaFocusLost(evt);
            }
        });
        relayInformScrollPanel.setViewportView(createRelayInformTextArea);

        createRelayPanel.add(relayInformScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 185, 530, 90));

        createRelayTitleField.setBackground(new java.awt.Color(255,255,255,0));
        createRelayTitleField.setFont(new java.awt.Font("나눔스퀘어", 0, 12)); // NOI18N
        createRelayTitleField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createRelayTitleField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                createRelayTitleFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                createRelayTitleFieldFocusLost(evt);
            }
        });
        createRelayPanel.add(createRelayTitleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 75, 530, 30));

        relayFirstScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        relayFirstScrollPanel.setBorder(null);
        relayFirstScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        relayFirstScrollPanel.setToolTipText("");
        relayFirstScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        relayFirstScrollPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                relayFirstScrollPanelMouseWheelMoved(evt);
            }
        });

        relayFirstTable.setBackground(new java.awt.Color(255,255,255,0));
        relayFirstTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        relayFirstTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "순위", "커버", "곡/앨범", "가수"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        relayFirstTable.setMinimumSize(new java.awt.Dimension(10, 400));
        relayFirstTable.setOpaque(false);
        relayFirstTable.setRowHeight(80);
        relayFirstTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        relayFirstTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        relayFirstTable.getTableHeader().setResizingAllowed(false);
        relayFirstTable.getTableHeader().setReorderingAllowed(false);
        relayFirstTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                relayFirstTableMouseClicked(evt);
            }
        });
        relayFirstScrollPanel.setViewportView(relayFirstTable);
        /* ChartTable 기본 디자인 세팅 */
        JTableSetting.tableInit(relayFirstScrollPanel, relayFirstTable);
        JTableSetting.tableHeaderInit(relayFirstTable, relayFirstScrollPanel.getWidth(), 40);
        JTableSetting.songTableSetting(relayFirstTable);

        createRelayPanel.add(relayFirstScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 349, 896, 305));

        addSongBtn.setBackground(new java.awt.Color(255,255,255,0));
        addSongBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/addTrack_btn.png"))); // NOI18N
        addSongBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addSongBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addSongBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addSongBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addSongBtnMouseExited(evt);
            }
        });
        createRelayPanel.add(addSongBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 310, 57, 22));

        createRelayBtn.setBackground(new java.awt.Color(255,255,255,0));
        createRelayBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/playCreate_btn.png"))); // NOI18N
        createRelayBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createRelayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createRelayBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createRelayBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                createRelayBtnMouseExited(evt);
            }
        });
        createRelayPanel.add(createRelayBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 310, 57, 22));

        createRelayInformLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/field/normal/playInform_field.png"))); // NOI18N
        createRelayPanel.add(createRelayInformLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 175, 568, 109));

        createRelayTitleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/field/normal/playTitle_field.png"))); // NOI18N
        createRelayPanel.add(createRelayTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 70, 568, 42));
        createRelayPanel.add(createRelayImgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 24, 260, 260));

        createRelayBGLabel.setBackground(new java.awt.Color(255,255,255,0));
        createRelayBGLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/background/createRelaylistBG.png"))); // NOI18N
        createRelayPanel.add(createRelayBGLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 912, 362));

        BackgroundPanel.add(createRelayPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(168, 60, 912, 660));
        panelList.add(createRelayPanel);

        createPlayPanel.setBackground(new java.awt.Color(255, 255, 255));
        createPlayPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        playInformScrollPanel.setBackground(new java.awt.Color(255, 255, 255));
        playInformScrollPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        createPlayInformTextArea.setBackground(new java.awt.Color(255, 255, 255));
        createPlayInformTextArea.setColumns(20);
        createPlayInformTextArea.setFont(new java.awt.Font("나눔스퀘어", 0, 12)); // NOI18N
        createPlayInformTextArea.setRows(5);
        createPlayInformTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPlayInformTextArea.setOpaque(false);
        createPlayInformTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                createPlayInformTextAreaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                createPlayInformTextAreaFocusLost(evt);
            }
        });
        playInformScrollPanel.setViewportView(createPlayInformTextArea);

        createPlayPanel.add(playInformScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 185, 530, 90));

        playBsideScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        playBsideScrollPanel.setBorder(null);
        playBsideScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        playBsideScrollPanel.setToolTipText("");
        playBsideScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        playBsideScrollPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                playBsideScrollPanelMouseWheelMoved(evt);
            }
        });

        playBsideTable.setBackground(new java.awt.Color(255,255,255,0));
        playBsideTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        playBsideTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "순위", "커버", "곡/앨범", "가수"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        playBsideTable.setMinimumSize(new java.awt.Dimension(10, 400));
        playBsideTable.setOpaque(false);
        playBsideTable.setRowHeight(80);
        playBsideTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        playBsideTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        playBsideTable.getTableHeader().setResizingAllowed(false);
        playBsideTable.getTableHeader().setReorderingAllowed(false);
        playBsideTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playBsideTableMouseClicked(evt);
            }
        });
        playBsideScrollPanel.setViewportView(playBsideTable);
        /* ChartTable 기본 디자인 세팅 */
        JTableSetting.tableInit(playBsideScrollPanel, playBsideTable);
        JTableSetting.tableHeaderInit(playBsideTable, playBsideScrollPanel.getWidth(), 40);
        JTableSetting.songTableSetting(playBsideTable);

        createPlayPanel.add(playBsideScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 349, 896, 305));

        createPlayTitleField.setBackground(new java.awt.Color(255,255,255,0));
        createPlayTitleField.setFont(new java.awt.Font("나눔스퀘어", 0, 12)); // NOI18N
        createPlayTitleField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPlayTitleField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                createPlayTitleFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                createPlayTitleFieldFocusLost(evt);
            }
        });
        createPlayPanel.add(createPlayTitleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 75, 530, 30));

        createPlayBtn.setBackground(new java.awt.Color(255,255,255,0));
        createPlayBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/playCreate_btn.png"))); // NOI18N
        createPlayBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPlayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createPlayBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createPlayBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                createPlayBtnMouseExited(evt);
            }
        });
        createPlayPanel.add(createPlayBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 310, 57, 22));

        addTrackBtn.setBackground(new java.awt.Color(255,255,255,0));
        addTrackBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/addTrack_btn.png"))); // NOI18N
        addTrackBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addTrackBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addTrackBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addTrackBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addTrackBtnMouseExited(evt);
            }
        });
        createPlayPanel.add(addTrackBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 310, 57, 22));
        createPlayPanel.add(createPlayImgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 25, 260, 260));

        createPlayTitleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/field/normal/playTitle_field.png"))); // NOI18N
        createPlayPanel.add(createPlayTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 70, 568, 42));

        createPlayInformLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/field/normal/playInform_field.png"))); // NOI18N
        createPlayPanel.add(createPlayInformLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 175, 568, 109));

        createPlayBGLabel.setBackground(new java.awt.Color(255,255,255,0));
        createPlayBGLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/background/createPlaylistBG.png"))); // NOI18N
        createPlayPanel.add(createPlayBGLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 912, 362));

        BackgroundPanel.add(createPlayPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(168, 60, 912, 660));
        panelList.add(createPlayPanel);

        relaylistDetailPanel.setBackground(new java.awt.Color(255, 255, 255));
        relaylistDetailPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        relaylistInformLabel.setFont(new java.awt.Font("AppleSDGothicNeoSB00", 0, 14)); // NOI18N
        relaylistInformLabel.setForeground(new java.awt.Color(204, 204, 204));
        relaylistInformLabel.setText("도토리를 훔쳐간 싸이월드 BGM");
        relaylistInformLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        relaylistDetailPanel.add(relaylistInformLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(357, 140, 530, 40));

        firstSongImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/test/younha.jpg"))); // NOI18N
        relaylistDetailPanel.add(firstSongImageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 250, 250));

        firstSongTitleLabel.setFont(new java.awt.Font("AppleSDGothicNeoSB00", 0, 18)); // NOI18N
        firstSongTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
        firstSongTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        firstSongTitleLabel.setText("오늘 헤어 졌어요");
        relaylistDetailPanel.add(firstSongTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 225, 540, 30));

        relaylistTitleLabel.setFont(new java.awt.Font("나눔스퀘어 Bold", 1, 36)); // NOI18N
        relaylistTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
        relaylistTitleLabel.setText("<html><p>도토리를 훔쳐간<br>싸이월드 BGM</p></html>");
        relaylistTitleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        relaylistDetailPanel.add(relaylistTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(355, 50, 530, 100));

        firstSongSingerLabel.setFont(new java.awt.Font("AppleSDGothicNeoSB00", 0, 16)); // NOI18N
        firstSongSingerLabel.setForeground(new java.awt.Color(204, 204, 204));
        firstSongSingerLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        firstSongSingerLabel.setText("윤하");
        firstSongSingerLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        relaylistDetailPanel.add(firstSongSingerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 255, 540, 20));

        blurLabel.setBackground(new java.awt.Color(0,0,0,0));
        blurLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/background/blur.png"))); // NOI18N
        relaylistDetailPanel.add(blurLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 912, 290));

        relayImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/test/younha.jpg"))); // NOI18N
        relaylistDetailPanel.add(relayImageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 910, 290));

        relayDetailScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        relayDetailScrollPanel.setBorder(null);
        relayDetailScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        relayDetailScrollPanel.setToolTipText("");
        relayDetailScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        relayDetailScrollPanel.setOpaque(false);
        relayDetailScrollPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                relayDetailScrollPanelMouseWheelMoved(evt);
            }
        });

        relayDetailTable.setBackground(new java.awt.Color(255,255,255,0));
        relayDetailTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        relayDetailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "번호", "커버", "곡/앨범", "가수"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        relayDetailTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        relayDetailTable.setMinimumSize(new java.awt.Dimension(10, 400));
        relayDetailTable.setOpaque(false);
        relayDetailTable.setRowHeight(80);
        relayDetailTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        relayDetailTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        relayDetailTable.getTableHeader().setResizingAllowed(false);
        relayDetailTable.getTableHeader().setReorderingAllowed(false);
        relayDetailTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                relayDetailTableMouseClicked(evt);
            }
        });
        relayDetailScrollPanel.setViewportView(relayDetailTable);
        /* PlayDetailTable 기본 디자인 세팅 */
        JTableSetting.tableInit(relaylistScrollPanel, relayDetailTable);
        JTableSetting.tableHeaderInit(relayDetailTable, relaylistScrollPanel.getWidth(), 40);
        JTableSetting.songTableSetting(relayDetailTable);

        relaylistDetailPanel.add(relayDetailScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 300, 900, 350));

        BackgroundPanel.add(relaylistDetailPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(168, 60, 912, 660));
        panelList.add(relaylistDetailPanel);

        playlistDetailPanel.setBackground(new java.awt.Color(255, 255, 255));
        playlistDetailPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        playDeleteBtn.setBackground(new java.awt.Color(255,255,255,0));
        playDeleteBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/delete_btn.png"))); // NOI18N
        playDeleteBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        playDeleteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playDeleteBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                playDeleteBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                playDeleteBtnMouseExited(evt);
            }
        });
        playlistDetailPanel.add(playDeleteBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 10, 57, 22));

        playEditBtn.setBackground(new java.awt.Color(255,255,255,0));
        playEditBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/layout/button/normal/edit_btn.png"))); // NOI18N
        playEditBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        playEditBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                playEditBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                playEditBtnMouseExited(evt);
            }
        });
        playlistDetailPanel.add(playEditBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 10, 57, 22));

        playImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/test/younha.jpg"))); // NOI18N
        playlistDetailPanel.add(playImageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 260, 260));

        playTitleLabel.setFont(new java.awt.Font("나눔스퀘어 Bold", 0, 36)); // NOI18N
        playTitleLabel.setForeground(new java.awt.Color(0, 0, 0));
        playTitleLabel.setText("윤하 노래 모음");
        playlistDetailPanel.add(playTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 20, 520, 50));

        playInformLabel.setFont(new java.awt.Font("AppleSDGothicNeoB00", 0, 18)); // NOI18N
        playInformLabel.setText("<html>초저녁 감성</html>");
        playInformLabel.setForeground(new Color(187,187,187));
        playInformLabel.setVerticalAlignment(JLabel.TOP);
        playlistDetailPanel.add(playInformLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 100, 530, 90));

        playDateLabel.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 18)); // NOI18N
        playDateLabel.setText("2022-05-22");
        playDateLabel.setForeground(new Color(187,187,187));
        playlistDetailPanel.add(playDateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 70, 510, 20));

        playAuthorLabel.setFont(new java.awt.Font("AppleSDGothicNeoB00", 0, 18)); // NOI18N
        playAuthorLabel.setForeground(new java.awt.Color(87, 144, 255));
        playAuthorLabel.setText("by 랄로(Ralo)");
        playlistDetailPanel.add(playAuthorLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 245, 410, 30));

        playIdLabel.setForeground(new java.awt.Color(255, 255, 255));
        playlistDetailPanel.add(playIdLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(748, 270, 150, -1));

        playDetailScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        playDetailScrollPanel.setBorder(null);
        playDetailScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        playDetailScrollPanel.setToolTipText("");
        playDetailScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        playDetailScrollPanel.setOpaque(false);
        playDetailScrollPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                playDetailScrollPanelMouseWheelMoved(evt);
            }
        });

        playDetailTable.setBackground(new java.awt.Color(255,255,255,0));
        playDetailTable.setOpaque(false);
        playDetailTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        playDetailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "번호", "커버", "곡/앨범", "가수"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        playDetailTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        playDetailTable.setMinimumSize(new java.awt.Dimension(10, 400));
        playDetailTable.setOpaque(false);
        playDetailTable.setRowHeight(80);
        playDetailTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        playDetailTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        playDetailTable.getTableHeader().setResizingAllowed(false);
        playDetailTable.getTableHeader().setReorderingAllowed(false);
        playDetailTable.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                playDetailTableMouseWheelMoved(evt);
            }
        });
        playDetailTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playDetailTableMouseClicked(evt);
            }
        });
        playDetailScrollPanel.setViewportView(playDetailTable);
        /* PlayDetailTable 기본 디자인 세팅 */
        JTableSetting.tableInit(playlistScrollPanel, playDetailTable);
        JTableSetting.tableHeaderInit(playDetailTable, playlistScrollPanel.getWidth(), 40);
        JTableSetting.songTableSetting(playDetailTable);

        playlistDetailPanel.add(playDetailScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 300, 900, 350));

        BackgroundPanel.add(playlistDetailPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(168, 60, 912, 660));
        panelList.add(playlistDetailPanel);

        chartPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartPanel.setOpaque(false);
        chartPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        chartScrollPanel.setBackground(new java.awt.Color(255,255,255,0)
        );
        chartScrollPanel.setBorder(null);
        chartScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chartScrollPanel.setToolTipText("");
        chartScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        chartScrollPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                chartScrollPanelMouseWheelMoved(evt);
            }
        });

        chartTable.setBackground(new java.awt.Color(255,255,255,0));
        chartTable.setFont(new java.awt.Font("AppleSDGothicNeoR00", 0, 14)); // NOI18N
        chartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "순위", "커버", "곡/앨범", "가수"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        chartTable.setMinimumSize(new java.awt.Dimension(10, 400));
        chartTable.setOpaque(false);
        chartTable.setRowHeight(80);
        chartTable.setSelectionBackground(new java.awt.Color(216, 229, 255));
        chartTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        chartTable.getTableHeader().setResizingAllowed(false);
        chartTable.getTableHeader().setReorderingAllowed(false);
        chartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chartTableMouseClicked(evt);
            }
        });
        chartScrollPanel.setViewportView(chartTable);
        /* ChartTable 기본 디자인 세팅 */
        JTableSetting.tableInit(chartScrollPanel, chartTable);
        JTableSetting.tableHeaderInit(chartTable, chartScrollPanel.getWidth(), 40);
        JTableSetting.songTableSetting(chartTable);

        chartPanel.add(chartScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 125, 896, 529));

        BackgroundPanel.add(chartPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(168, 60, 910, 660));
        panelList.add(chartPanel);

        notifyPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout notifyPanelLayout = new javax.swing.GroupLayout(notifyPanel);
        notifyPanel.setLayout(notifyPanelLayout);
        notifyPanelLayout.setHorizontalGroup(
            notifyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 910, Short.MAX_VALUE)
        );
        notifyPanelLayout.setVerticalGroup(
            notifyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 660, Short.MAX_VALUE)
        );

        BackgroundPanel.add(notifyPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, 910, 660));
        panelList.add(notifyPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(BackgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(BackgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /* 홈 메뉴 마우스 이벤트 */
    private void HomeLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeLabelMouseEntered
        // TODO add your handling code here:
        HomeLabel.setIcon(new ImageIcon("./src/resources/layout/menu/hover/home_hover.png"));
        HomeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_HomeLabelMouseEntered

    private void HomeLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeLabelMouseExited
        // TODO add your handling code here:
        HomeLabel.setIcon(new ImageIcon("./src/resources/layout/menu/normal/home.png"));
        HomeLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_HomeLabelMouseExited

    /* 플레이리스트 메뉴 마우스 이벤트 */
    private void PlaylistLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlaylistLabelMouseEntered
        // TODO add your handling code here:
        PlaylistLabel.setIcon(new ImageIcon("./src/resources/layout/menu/hover/playlist_hover.png"));
        PlaylistLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_PlaylistLabelMouseEntered

    private void PlaylistLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlaylistLabelMouseExited
        // TODO add your handling code here:
        PlaylistLabel.setIcon(new ImageIcon("./src/resources/layout/menu/normal/playlist.png"));
        PlaylistLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_PlaylistLabelMouseExited

    /* 릴레이리스트 메뉴 마우스 이벤트 */
    private void RelaylistLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RelaylistLabelMouseEntered
        // TODO add your handling code here:
        RelaylistLabel.setIcon(new ImageIcon("./src/resources/layout/menu/hover/relaylist_hover.png"));
        RelaylistLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_RelaylistLabelMouseEntered

    private void RelaylistLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RelaylistLabelMouseExited
        // TODO add your handling code here:
        RelaylistLabel.setIcon(new ImageIcon("./src/resources/layout/menu/normal/relaylist.png"));
        RelaylistLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_RelaylistLabelMouseExited

    /* 알림 메뉴 마우스 이벤트 */
    private void NotifyLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NotifyLabelMouseEntered
        // TODO add your handling code here:
        NotifyLabel.setIcon(new ImageIcon("./src/resources/layout/menu/hover/notify_hover.png"));
        NotifyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_NotifyLabelMouseEntered

    private void NotifyLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NotifyLabelMouseExited
        // TODO add your handling code here:
        NotifyLabel.setIcon(new ImageIcon("./src/resources/layout/menu/normal/notify.png"));
        NotifyLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_NotifyLabelMouseExited

    /* 스크롤 패널 스크롤 이벤트 구현 */
    private void chartScrollPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_chartScrollPanelMouseWheelMoved
        // TODO add your handling code here:
        JTableSetting.tableScroll(chartTable, chartScrollPanel, evt);
    }//GEN-LAST:event_chartScrollPanelMouseWheelMoved

    private void HomeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeLabelMouseClicked
        // TODO add your handling code here:
        JPanelSetting.changePanel(this.panelList, this.chartPanel);
    }//GEN-LAST:event_HomeLabelMouseClicked

    // 릴레이리스트 목록 조회 이벤트
    private void RelaylistLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RelaylistLabelMouseClicked
        getAllRelaylists(); // 릴레이리스트 목록 조회 메소드
    }//GEN-LAST:event_RelaylistLabelMouseClicked

    // 플레이리스트 조회 이벤트
    private void PlaylistLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlaylistLabelMouseClicked
        // TODO add your handling code here:
        JPanelSetting.changePanel(this.panelList, this.playlistPanel);
        
        DefaultTableModel model = (DefaultTableModel) playlistTable.getModel();
        model.setRowCount(0);
        
        // 플레이리스트를 전부 가져옴
        ArrayList<PlaylistDto> playlist = playlistController.getAllPlaylists();

        Object[][] data = new Object[playlist.size()][];
        
        for (int i = 0; i < playlist.size(); i++) {
            // 플레이리스트 정보 추출
            String playlistId = playlist.get(i).getId();
            String title = playlist.get(i).getTitle();
            String author = playlist.get(i).getAuthor();
            String inform = playlist.get(i).getInform();
            String imageUrl = playlist.get(i).getImage();
            ImageIcon image = ComponentSetting.imageToIcon(imageUrl, 100, 100);
            java.sql.Date createTime = playlist.get(i).getCreateTime();
            
            data[i] = new Object[]{
                model.getRowCount() + (i + 1),
                image,
                convertListToHtml(playlistId, title, author, inform),
                createTime
            };
        }
        
        // 테이블에 플레이리스트 삽입
        JTableSetting.insertTableRow((DefaultTableModel) playlistTable.getModel(), data);
        
    }//GEN-LAST:event_PlaylistLabelMouseClicked

    private void NotifyLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NotifyLabelMouseClicked
        // TODO add your handling code here:
        JPanelSetting.changePanel(this.panelList, this.notifyPanel);
    }//GEN-LAST:event_NotifyLabelMouseClicked

    private void playDetailScrollPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_playDetailScrollPanelMouseWheelMoved
        // TODO add your handling code here:
        JTableSetting.tableScroll(playlistTable, playlistScrollPanel, evt);
    }//GEN-LAST:event_playDetailScrollPanelMouseWheelMoved

    private void playDetailTableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_playDetailTableMouseWheelMoved
        // TODO add your handling code here:
        JTableSetting.tableScroll(playDetailTable, playDetailScrollPanel, evt);
    }//GEN-LAST:event_playDetailTableMouseWheelMoved

    private void playDetailTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playDetailTableMouseClicked
        // TODO add your handling code here:
        int row = this.playDetailTable.getSelectedRow();
        int column = this.playDetailTable.getSelectedColumn();
        TableModel model = this.playDetailTable.getModel();

        Document doc = Jsoup.parse(model.getValueAt(row, 2).toString());

        Element element = doc.select("body").get(0);

        System.out.println("엘리먼트 " + element);
        System.out.println("아이디 : " + element.select("input").attr("value"));
        System.out.println("제목 : " + element.select("#title").text());
        System.out.println("앨범 : " + element.select("#album").text());

        System.out.println(row + "행, " + column + "열 : " + model.getValueAt(row, 2) + " 선택했음");
    }//GEN-LAST:event_playDetailTableMouseClicked

    // 플레이리스트 상세 조회
    private void playlistTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistTableMouseClicked
        // TODO add your handling code here:
        // 선택 된 행의 플레이리스트 ID를 가져와서 컨트롤러에게 플레이리스트 정보를 요청
        // 전달받은 플레이리스트의 정보를 playDetailPanel로 전달하여 출력해야 함
        
        // 플레이리스트 상세 조회 페이지 초기화
        initPlayDetialPanel();
        
        int row = this.playlistTable.getSelectedRow();
        if (row < 0) return;    // 빈 공간을 선택한 경우 
        
        TableModel model = this.playlistTable.getModel();
        
        // 플레이리스트 아이디 가져오기
        Document doc = Jsoup.parse(model.getValueAt(row, 2).toString());
        
        Element element = doc.selectFirst("body");
        String listId = element.select("#listId").attr("value");
        
        //1. 플레이리스트 아이디로 플레이리스트 가져오기
        PlaylistDto playlist = playlistController.getPlaylist(listId);
        
        //2. 수록곡 가져오기
        ArrayList<SongDto> sideTrack = songController.getBsideTrack("playBsideTrack", listId);
        
        //3. 플레이리스트 조회 화면 설정
        playImageLabel.setIcon(ComponentSetting.imageToIcon(playlist.getImage(), 260, 260)); // 썸네일 지정
        playTitleLabel.setText(playlist.getTitle());                                         // 제목 지정
        playDateLabel.setText(playlist.getCreateTime().toString());                          // 생성 날짜 지정
        playInformLabel.setText(playlist.getInform());                                       // 설명 지정
        playAuthorLabel.setText(playlist.getAuthor());                                       // 작성자 지정
        playIdLabel.setText(playlist.getId());                                               // 플레이리스트 아이디 지정
        
        //4. 수록곡 테이블에 노래 삽입
        Object[][] value = DataParser.songDtoToObject(sideTrack);
        JTableSetting.insertTableRow((DefaultTableModel) playDetailTable.getModel(), value);
        
        JPanelSetting.changePanel(panelList, playlistDetailPanel);
    }//GEN-LAST:event_playlistTableMouseClicked
    
    private void relayDetailTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_relayDetailTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_relayDetailTableMouseClicked

    private void relayDetailScrollPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_relayDetailScrollPanelMouseWheelMoved
        // TODO add your handling code here:
        JTableSetting.tableScroll(relayDetailTable, relayDetailScrollPanel, evt);
    }//GEN-LAST:event_relayDetailScrollPanelMouseWheelMoved

    private void playlistTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistTableMousePressed
        // TODO add your handling code here:
        this.playlistTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_playlistTableMousePressed

    // 릴레이리스트 상세 조회 이벤트
    private void relaylistTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_relaylistTableMouseClicked
        // 릴레이리스트 상세 조회 페이지 초기화
        initRelayDetialPanel();
        
        int row = relaylistTable.getSelectedRow();
        if (row < 0) return;
        
        DefaultTableModel model = (DefaultTableModel) relaylistTable.getModel();
        
        // 릴레이리스트 아이디 가져오기
        Document doc = Jsoup.parse(model.getValueAt(row, 2).toString());
        
        Element element = doc.selectFirst("body");
        String listId = element.select("#listId").attr("value");
        
        // 1. 릴레이리스트 아이디로 릴레이리스트 가져오기
        RelaylistDto relaylist = relaylistController.getRelaylist(listId);
        
        // 2. 릴레이리스트 조회 화면 세팅
        firstSongImageLabel.setIcon(ComponentSetting.imageToIcon(relaylist.getFirstSongImage(), 260, 260));     // 썸네일 지정
        relayImageLabel.setIcon(ComponentSetting.getBigBlurImage(relaylist.getFirstSongImage()));               // 배경 지정
        relaylistTitleLabel.setText("<html><p>" + relaylist.getTitle().replace("\n", "<br>")+ "</p></html>");   // 제목 지정
        relaylistInformLabel.setText(relaylist.getInform());
        firstSongTitleLabel.setText(relaylist.getFirstSongTitle());                                             // 첫 곡 제목 지정
        firstSongSingerLabel.setText(relaylist.getFirstSongSinger());                                           // 첫 곡 가수 지정
        
        //4. 수록곡 테이블에 노래 삽입
        ArrayList<SongDto> sideTrack = songController.getBsideTrack("relayBsideTrack", listId);
        Object[][] value = DataParser.songDtoToObject(sideTrack);
        
        JTableSetting.insertTableRow((DefaultTableModel) relayFirstTable.getModel(), value);
        
        JPanelSetting.changePanel(panelList, relaylistDetailPanel);
        
    }//GEN-LAST:event_relaylistTableMouseClicked

    /* 프레임 로딩 이벤트 (인기차트 조회) */
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:

        /* 인기차트 */
        songController.updateSongChart();
        ArrayList<SongChart> chart = songController.getSongChart();
        Object[][] values = songChartToObject(chart);

        /* 테이블에 값 추가*/
        JTableSetting.insertTableRow((DefaultTableModel) chartTable.getModel(), values);
    }//GEN-LAST:event_formWindowOpened

    /* 인기차트 테이블 마우스 클릭 이벤트 */
    private void chartTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chartTableMouseClicked
        // TODO add your handling code here:
        int row = this.chartTable.getSelectedRow();
        int column = this.chartTable.getSelectedColumn();
        TableModel model = this.chartTable.getModel();

        System.out.println(row + "행, " + column + "열 : " + model.getValueAt(row, 2) + " 선택했음");
    }//GEN-LAST:event_chartTableMouseClicked

    /* 플레이리스트 제목 생성 필드 이벤트 */
    private void createPlayTitleFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createPlayTitleFieldFocusGained
        // TODO add your handling code here:
        this.createPlayTitleLabel.setIcon(new ImageIcon("./src/resources/layout/field/focus/playTitle_field_focus.png"));
    }//GEN-LAST:event_createPlayTitleFieldFocusGained

    private void createPlayTitleFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createPlayTitleFieldFocusLost
        // TODO add your handling code here:
        this.createPlayTitleLabel.setIcon(new ImageIcon("./src/resources/layout/field/normal/playTitle_field.png"));
    }//GEN-LAST:event_createPlayTitleFieldFocusLost

    /* 플레이리스트 설명 생성 필드 이벤트 */
    private void createPlayInformTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createPlayInformTextAreaFocusGained
        // TODO add your handling code here:
        this.createPlayInformLabel.setIcon(new ImageIcon("./src/resources/layout/field/focus/playInform_field_focus.png"));
    }//GEN-LAST:event_createPlayInformTextAreaFocusGained

    private void createPlayInformTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createPlayInformTextAreaFocusLost
        // TODO add your handling code here:
        this.createPlayInformLabel.setIcon(new ImageIcon("./src/resources/layout/field/normal/playInform_field.png"));
    }//GEN-LAST:event_createPlayInformTextAreaFocusLost

    private void playBsideTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playBsideTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_playBsideTableMouseClicked

    private void playBsideScrollPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_playBsideScrollPanelMouseWheelMoved
        // TODO add your handling code here:
        JTableSetting.tableScroll(playBsideTable, playBsideScrollPanel, evt);
    }//GEN-LAST:event_playBsideScrollPanelMouseWheelMoved

    private void addTrackBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrackBtnMouseEntered
        // TODO add your handling code here:
        this.addTrackBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/addTrack_btn_hover.png"));
        this.addTrackBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addTrackBtnMouseEntered

    private void addTrackBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrackBtnMouseExited
        // TODO add your handling code here:
        this.addTrackBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/addTrack_btn.png"));
    }//GEN-LAST:event_addTrackBtnMouseExited

    private void createPlayBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createPlayBtnMouseEntered
        // TODO add your handling code here:
        this.createPlayBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/playCreate_btn_hover.png"));
        this.createPlayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_createPlayBtnMouseEntered

    private void createPlayBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createPlayBtnMouseExited
        // TODO add your handling code here:
        this.createPlayBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/playCreate_btn.png"));
    }//GEN-LAST:event_createPlayBtnMouseExited

    private void addPlaylistBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addPlaylistBtnMouseEntered
        // TODO add your handling code here:
        this.addPlaylistBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/addTrack_btn_hover.png"));
        this.addPlaylistBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addPlaylistBtnMouseEntered

    private void addPlaylistBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addPlaylistBtnMouseExited
        // TODO add your handling code here:
        this.addPlaylistBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/addTrack_btn.png"));
    }//GEN-LAST:event_addPlaylistBtnMouseExited

    private void addPlaylistBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addPlaylistBtnMouseClicked
        // TODO add your handling code here:
        // 플레이리스트 생성 페이지 초기화
        this.createPlayImgLabel.setIcon(null);
        this.createPlayTitleField.setText("");
        this.createPlayInformTextArea.setText("");
        ((DefaultTableModel) this.playBsideTable.getModel()).setRowCount(0);
        
        JPanelSetting.changePanel(panelList, createPlayPanel);
    }//GEN-LAST:event_addPlaylistBtnMouseClicked

    private void addTrackBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrackBtnMouseClicked
        // TODO add your handling code here:
        SearchFrame search = new SearchFrame();
        
        search.setBsideTable(this.playBsideTable);
        search.setCreatePlayImgLabel(this.createPlayImgLabel);
        search.setIsRelayList(false);
    }//GEN-LAST:event_addTrackBtnMouseClicked

    /* 플레이리스트 생성 완료 버튼 */
    private void createPlayBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createPlayBtnMouseClicked
        
        // 플레이리스트 제목, 설명, 이미지, 작성자, 수록곡 필요
        TableModel tm = playBsideTable.getModel();
        int row = tm.getRowCount();
        
        if(row == 0) return;    // 한 곡도 선택하지 않은 경우
        
        // 제목, 가수, 이미지, 앨범 필요
        // 선택한 곡을 Songlist로 변환
        ArrayList<SongCreateDto> songlist = new ArrayList<>();        
        for (int i = 0; i < row; i++) songlist.add(DataParser.parseHtmlToSong(tm.getValueAt(i, 2)));
        
        // 1. 선택한 곡 Song 테이블에 저장
        ArrayList<SongDto> songDtolist = songController.addSongList(songlist);
        
        // 2. 플레이리스트 저장
        PlaylistCreateDto playlistCreateDto = PlaylistCreateDto.builder()
                                    .title(createPlayTitleField.getText())
                                    .inform(createPlayInformTextArea.getText())
                                    .author(LoginUserLabel.getText())
                                    .image(songlist.get(0).getImage())
                                    .createTime(new java.sql.Date(new java.util.Date().getTime()))
                                    .build();
        
        PlaylistDto playlistDto = playlistController.createPlaylist(playlistCreateDto);
        
        // 3. 수록곡 저장
        ArrayList<PlayBsideTrackDto> bSideTrackDto = new ArrayList<>();
        
        for (SongDto songDto : songDtolist) {
            bSideTrackDto.add(PlayBsideTrackDto.builder()
                                               .playlistId(playlistDto.getId())
                                               .songId(songDto.getId())
                                               .build());
        }
        
        playBsideTrackController.addPlayBsideTrack(bSideTrackDto);
        
        JOptionPane.showMessageDialog(null, "플레이리스트 생성이 완료 되었습니다.");
    }//GEN-LAST:event_createPlayBtnMouseClicked

    private void playDeleteBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playDeleteBtnMouseEntered
        // TODO add your handling code here:
        this.playDeleteBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/delete_btn_hover.png"));
        this.playDeleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_playDeleteBtnMouseEntered

    private void playDeleteBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playDeleteBtnMouseExited
        // TODO add your handling code here:
        this.playDeleteBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/delete_btn.png"));
    }//GEN-LAST:event_playDeleteBtnMouseExited

    private void playEditBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playEditBtnMouseEntered
        // TODO add your handling code here:
        this.playEditBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/edit_btn_hover.png"));
        this.playEditBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_playEditBtnMouseEntered

    private void playEditBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playEditBtnMouseExited
        // TODO add your handling code here:
        this.playEditBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/edit_btn.png"));
    }//GEN-LAST:event_playEditBtnMouseExited

    private void playDeleteBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playDeleteBtnMouseClicked
        // TODO add your handling code here:
        boolean result = playlistController.deletePlaylist(playIdLabel.getText());
        
        if(result) JOptionPane.showMessageDialog(null, "플레이리스트가 삭제 되었습니다.");
        else JOptionPane.showMessageDialog(null, "플레이리스트 삭제에 실패 했습니다.");
    }//GEN-LAST:event_playDeleteBtnMouseClicked

    private void createRelayInformTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createRelayInformTextAreaFocusGained
        // TODO add your handling code here:
        this.createPlayInformLabel.setIcon(new ImageIcon("./src/resources/layout/field/focus/playInform_field_focus.png"));
    }//GEN-LAST:event_createRelayInformTextAreaFocusGained

    private void createRelayInformTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createRelayInformTextAreaFocusLost
        // TODO add your handling code here:
        this.createPlayInformLabel.setIcon(new ImageIcon("./src/resources/layout/field/normal/playInform_field.png"));
    }//GEN-LAST:event_createRelayInformTextAreaFocusLost

    private void createRelayTitleFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createRelayTitleFieldFocusGained
        // TODO add your handling code here:
         this.createRelayTitleLabel.setIcon(new ImageIcon("./src/resources/layout/field/focus/playTitle_field_focus.png"));
    }//GEN-LAST:event_createRelayTitleFieldFocusGained

    private void createRelayTitleFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_createRelayTitleFieldFocusLost
        // TODO add your handling code here:
        this.createRelayTitleLabel.setIcon(new ImageIcon("./src/resources/layout/field/normal/playTitle_field.png"));
    }//GEN-LAST:event_createRelayTitleFieldFocusLost

    private void relayFirstTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_relayFirstTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_relayFirstTableMouseClicked

    private void relayFirstScrollPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_relayFirstScrollPanelMouseWheelMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_relayFirstScrollPanelMouseWheelMoved

    private void addSongBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSongBtnMouseClicked
        // TODO add your handling code here:
        SearchFrame search = new SearchFrame();
        
        search.setBsideTable(this.relayFirstTable);
        search.setCreatePlayImgLabel(this.createRelayImgLabel);
        search.setIsRelayList(true);
    }//GEN-LAST:event_addSongBtnMouseClicked

    private void addSongBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSongBtnMouseEntered
        // TODO add your handling code here:
        this.addSongBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/addTrack_btn_hover.png"));
        this.addSongBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addSongBtnMouseEntered

    private void addSongBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSongBtnMouseExited
        // TODO add your handling code here:
        this.addSongBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/addTrack_btn.png"));
    }//GEN-LAST:event_addSongBtnMouseExited

    // 릴레이리스트 생성 버튼
    private void createRelayBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createRelayBtnMouseClicked
        createRelaylist();  //릴레이리스트 생성 메소드
    }//GEN-LAST:event_createRelayBtnMouseClicked

    private void createRelayBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createRelayBtnMouseEntered
        // TODO add your handling code here:
        this.createRelayBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/playCreate_btn_hover.png"));
        this.createRelayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_createRelayBtnMouseEntered

    private void createRelayBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createRelayBtnMouseExited
        // TODO add your handling code here:
        this.createRelayBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/playCreate_btn.png"));
    }//GEN-LAST:event_createRelayBtnMouseExited

    private void addRelaylistBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addRelaylistBtnMouseClicked
        // TODO add your handling code here:
        this.createRelayImgLabel.setIcon(null);
        this.createRelayTitleField.setText("");
        this.createRelayInformTextArea.setText("");
        ((DefaultTableModel) this.relayFirstTable.getModel()).setRowCount(0);
        
        JPanelSetting.changePanel(panelList, createRelayPanel);
    }//GEN-LAST:event_addRelaylistBtnMouseClicked

    private void addRelaylistBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addRelaylistBtnMouseEntered
        // TODO add your handling code here:
        this.addRelaylistBtn.setIcon(new ImageIcon("./src/resources/layout/button/hover/addTrack_btn_hover.png"));
        this.addRelaylistBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addRelaylistBtnMouseEntered

    private void addRelaylistBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addRelaylistBtnMouseExited
        // TODO add your handling code here:
        this.addRelaylistBtn.setIcon(new ImageIcon("./src/resources/layout/button/normal/addTrack_btn.png"));
    }//GEN-LAST:event_addRelaylistBtnMouseExited
    
    // 플레이리스트 상세 조회 페이지 초기화
    private void initPlayDetialPanel(){
        playImageLabel.setIcon(null);               // 썸네일 지정
        playTitleLabel.setText("");                 // 제목 지정
        playDateLabel.setText("");                  // 생성 날짜 지정
        playInformLabel.setText("");                // 설명 지정
        playAuthorLabel.setText("");                // 작성자 지정
        
        DefaultTableModel tm = (DefaultTableModel) playDetailTable.getModel();
        tm.setRowCount(0);
    }
    
    // 릴레이리스트 상세 조회 페이지 초기화
    private void initRelayDetialPanel(){
        firstSongImageLabel.setIcon(null);
        relayImageLabel.setIcon(null);
        relaylistTitleLabel.setText("");
        relaylistInformLabel.setText("");
        firstSongTitleLabel.setText("");
        firstSongSingerLabel.setText("");
        
        DefaultTableModel tm = (DefaultTableModel) relayFirstTable.getModel();
        tm.setRowCount(0);
    }

    
    public Object[][] songChartToObject(ArrayList<SongChart> songArray) {
        Object[][] values = new Object[songArray.size()][];

        for (int i = 0; i < songArray.size(); i++) {
            SongChart song = songArray.get(i);

            values[i] = new Object[]{song.getId(), 
                                    ComponentSetting.imageToIcon(song.getImage(), 60, 60), 
                                    convertSongToHtml(song.getTitle(), song.getAlbum(), song.getImage(), song.getSinger()),
                                    song.getSinger() };
        }

        return values;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BackgroundPanel;
    private javax.swing.JLabel HeaderLabel;
    private javax.swing.JLabel HomeLabel;
    private javax.swing.JLabel LoginUserLabel;
    private javax.swing.JLabel NotifyLabel;
    private javax.swing.JLabel PlaylistLabel;
    private javax.swing.JLabel RelaylistLabel;
    private javax.swing.JLabel SidebarLabel;
    private javax.swing.JButton addPlaylistBtn;
    private javax.swing.JButton addRelaylistBtn;
    private javax.swing.JButton addSongBtn;
    private javax.swing.JButton addTrackBtn;
    private javax.swing.JLabel blurLabel;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JScrollPane chartScrollPanel;
    private javax.swing.JTable chartTable;
    private javax.swing.JLabel createPlayBGLabel;
    private javax.swing.JButton createPlayBtn;
    private javax.swing.JLabel createPlayImgLabel;
    private javax.swing.JLabel createPlayInformLabel;
    private javax.swing.JTextArea createPlayInformTextArea;
    private javax.swing.JPanel createPlayPanel;
    private javax.swing.JTextField createPlayTitleField;
    private javax.swing.JLabel createPlayTitleLabel;
    private javax.swing.JLabel createRelayBGLabel;
    private javax.swing.JButton createRelayBtn;
    private javax.swing.JLabel createRelayImgLabel;
    private javax.swing.JLabel createRelayInformLabel;
    private javax.swing.JTextArea createRelayInformTextArea;
    private javax.swing.JPanel createRelayPanel;
    private javax.swing.JTextField createRelayTitleField;
    private javax.swing.JLabel createRelayTitleLabel;
    private javax.swing.JLabel firstSongImageLabel;
    private javax.swing.JLabel firstSongSingerLabel;
    private javax.swing.JLabel firstSongTitleLabel;
    private javax.swing.JPanel notifyPanel;
    private javax.swing.JLabel playAuthorLabel;
    private javax.swing.JScrollPane playBsideScrollPanel;
    private javax.swing.JTable playBsideTable;
    private javax.swing.JLabel playDateLabel;
    private javax.swing.JButton playDeleteBtn;
    private javax.swing.JScrollPane playDetailScrollPanel;
    private javax.swing.JTable playDetailTable;
    private javax.swing.JButton playEditBtn;
    private javax.swing.JLabel playIdLabel;
    private javax.swing.JLabel playImageLabel;
    private javax.swing.JLabel playInformLabel;
    private javax.swing.JScrollPane playInformScrollPanel;
    private javax.swing.JLabel playTitleLabel;
    private javax.swing.JPanel playlistDetailPanel;
    private javax.swing.JPanel playlistPanel;
    private javax.swing.JScrollPane playlistScrollPanel;
    private javax.swing.JTable playlistTable;
    private javax.swing.JScrollPane relayDetailScrollPanel;
    private javax.swing.JTable relayDetailTable;
    private javax.swing.JScrollPane relayFirstScrollPanel;
    private javax.swing.JTable relayFirstTable;
    private javax.swing.JLabel relayImageLabel;
    private javax.swing.JScrollPane relayInformScrollPanel;
    private javax.swing.JPanel relaylistDetailPanel;
    private javax.swing.JLabel relaylistInformLabel;
    private javax.swing.JPanel relaylistPanel;
    private javax.swing.JScrollPane relaylistScrollPanel;
    private javax.swing.JTable relaylistTable;
    private javax.swing.JLabel relaylistTitleLabel;
    // End of variables declaration//GEN-END:variables
}
