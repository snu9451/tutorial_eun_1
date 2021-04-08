package miniproj.book;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MgrBoryu2 implements MouseListener, KeyListener {
	JFrame jf = null;
	JTextArea jta = new JTextArea();
	JTextField jtf = new JTextField();
	JLabel jlb = new JLabel("KOSMO 문고");
	JButton jbtn_sel = new JButton("검색");
	JButton jbtn_ins = new JButton("추가");
	JButton jbtn_del = new JButton("삭제");
	JButton jbtn_back = new JButton("이전");
	String[] combo = { "도서명", "도서번호", "저자", "출판사" };
	JComboBox<String> jcb = new JComboBox<String>(combo);
	String cols[] = { "도서번호", "분류", "도서명", "저자", "출판사", "출간일", "판매가격", "원가", "재고", "위치", "판매량", "이익" };
	String data[][] = new String[0][12];
	DefaultTableModel dtm_book = new DefaultTableModel(data, cols) {
		public boolean isCellEditable(int rowindex, int mcolindex) {
			return false;
		}
	};
	JTable jtb_book = new JTable(dtm_book);
	JScrollPane jsp_book = new JScrollPane(jtb_book);
	Font myfont = new Font(null, Font.BOLD, 50);
//   JScrollPane         jsp       = new JScrollPane(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	BookVO bVO = null;
	DBConnectionMgr dbMgr = null;
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	LoginView lv = null;
	static MgrBoryu2 mv = null;
	EditView ev = null;
	StringBuffer sql = new StringBuffer();
	Vector<BookVO> al = null;

	// 단위테스트용 임시 메인메소드///////////////////////////////////////////////////
	public static void main(String[] args) {
		new MgrBoryu2();
	}

	public MgrBoryu2() {
		loading();
		initDisplay();
	}

	public void initDisplay() {
		jf = new JFrame();
		jf.setResizable(false);
//      jtb_book.setEnabled(false);
		jlb.setFont(myfont);
		jf.getContentPane().setLayout(null);// 이 코드 넣으니까 된다 기존 레이아웃을 안 비워줘서 그대로 맞춰진건가봐
		jbtn_back.addMouseListener(this);
		jtb_book.addMouseListener(this);
		jbtn_sel.addMouseListener(this);
		jbtn_ins.addMouseListener(this);
		jlb.addMouseListener(this);
		jtf.addKeyListener(this);
		jbtn_del.addMouseListener(this);
		jtb_book.getTableHeader().setReorderingAllowed(false);
		jlb.setBounds(500, 50, 400, 60);
		jcb.setBounds(50, 140, 100, 20);
		jtf.setBounds(160, 140, 980, 20);
		jbtn_sel.setBounds(1150, 140, 100, 20);
		jbtn_ins.setBounds(670, 850, 180, 50);
		jbtn_del.setBounds(870, 850, 180, 50);
		jbtn_back.setBounds(1070, 850, 180, 50);
		jsp_book.setBounds(50, 170, 1200, 630);
		jf.add(jlb);
		jf.add(jcb);
		jf.add(jtf);
		jf.add(jbtn_sel);
		jf.add(jbtn_ins);
		jf.add(jbtn_del);
		jf.add(jbtn_back);
		jf.add("Center", jsp_book);
		jf.setSize(1300, 1000);
		jf.setTitle("관리자 페이지");
		jf.setVisible(true);
		jtf.grabFocus();
	}

	public void loading() {
		sql.setLength(0);
		sql.append("SELECT m.booknum, m.class, m.bookname, m.author, m.publisher, m.pdate, "
				+ "m.sprice, i.oprice, i.inventory, i.loc, i.sales, i.profit FROM bookmgr m, inventorytbl i "
				+ "WHERE m.booknum = i.booknum");
		select(sql, "");
	}

	public void select(StringBuffer sql, String q1) { // 조회 메소드
		dbMgr = DBConnectionMgr.getInstance();
		BookVO bVOS[] = null;
		try {
			con = dbMgr.getConnection();
			pstmt = con.prepareStatement(sql.toString());
			if (q1 != "") {
				pstmt.setString(1, q1);
			}
			rs = pstmt.executeQuery();
			al = new Vector<>();
			while (rs.next()) {
				bVO = new BookVO();
				bVO.setBooknum(rs.getString("booknum"));
				bVO.setParti(rs.getString("class"));
				bVO.setBookname(rs.getString("bookname"));
				bVO.setAuthor(rs.getString("author"));
				bVO.setPublisher(rs.getString("publisher"));
				bVO.setPdate(rs.getInt("pdate"));
				bVO.setSprice(rs.getInt("sprice"));
				bVO.setOprice(rs.getInt("oprice"));
				bVO.setInven(rs.getInt("inventory"));
				bVO.setLoc(rs.getString("loc"));
				bVO.setSales(rs.getInt("sales"));
				bVO.setProfit(rs.getInt("profit"));
				al.add(bVO);
			}
			bVOS = new BookVO[al.size()];
			// 벡터에 담긴 정보를 꺼내서 객체 배열에 초기화 하기
			al.copyInto(bVOS);
			while (dtm_book.getRowCount() > 0) {
				dtm_book.removeRow(0);
			}
			for (int i = 0; i < bVOS.length; i++) {
				Vector oneRow = new Vector();
				oneRow.add(bVOS[i].getBooknum());
				oneRow.add(bVOS[i].getParti());
				oneRow.add(bVOS[i].getBookname());
				oneRow.add(bVOS[i].getAuthor());
				oneRow.add(bVOS[i].getPublisher());
				oneRow.add(bVOS[i].getPdate());
				oneRow.add(bVOS[i].getSprice());
				oneRow.add(bVOS[i].getOprice());
				oneRow.add(bVOS[i].getInven());
				oneRow.add(bVOS[i].getLoc());
				oneRow.add(bVOS[i].getSales());
				oneRow.add(bVOS[i].getProfit());
				dtm_book.addRow(oneRow);
			}

		} catch (SQLException se) {
			// 부적합한 식별자 입니다.
			System.out.println("SQLExcption : " + se.getMessage());// 좀 더 구체적인 예외처리 클래스 정보를 알수 있다.
		}
	}

	public void sqlsetting() {
		if (jcb.getSelectedItem() == "도서명") {
//         combosel_sql ="m.bookname";
			sql.append(" where m.bookname like '%'||?||'%'   AND m.booknum = i.booknum");
		} else if (jcb.getSelectedItem() == "도서번호") {
//         combosel_sql ="m.booknum";
			sql.append(" where m.booknum like '%'||?||'%'   AND m.booknum = i.booknum");
		} else if (jcb.getSelectedItem() == "저자") {
//         combosel_sql ="m.author";
			sql.append(" where m.author like '%'||?||'%'   AND m.booknum = i.booknum");
		} else if (jcb.getSelectedItem() == "출판사") {
//         combosel_sql ="m.publisher";
			sql.append(" where m.publisher like '%'||?||'%'   AND m.booknum = i.booknum");
		}
	}

	@Override
   public void mouseClicked(MouseEvent e) {
      Object obj = e.getSource();
      if (e.getClickCount() == 1 && obj == jbtn_back) {
//         new EditView();
         jf.dispose();
         new LoginView();
      }
      else if(e.getClickCount() == 2 && obj == jtb_book) {
         int index[] = jtb_book.getSelectedRows();
         String booknum = dtm_book.getValueAt(index[0], 0).toString();
         String    sql = "SELECT m.booknum, m.class, m.bookname, m.author, m.publisher, m.pdate, m.sprice, i.oprice, i.inventory, i.loc, i.sales, i.profit"; 
               sql += " FROM bookmgr m, inventorytbl i";
               sql += " WHERE m.booknum = ? AND i.booknum = ?";
         BookVO    bVO = null;
         ev = new EditView();
         try {
            con = dbMgr.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, booknum);
            pstmt.setString(2, booknum);
            rs = pstmt.executeQuery();
            if(rs.next()) {
               bVO = new BookVO();
               bVO.setBooknum(rs.getString("booknum"));
               bVO.setParti(rs.getString("class"));
               bVO.setBookname(rs.getString("bookname"));
               bVO.setAuthor(rs.getString("author"));
               bVO.setPublisher(rs.getString("publisher"));
               bVO.setPdate(rs.getInt("pdate"));
               bVO.setSprice(rs.getInt("sprice"));
               bVO.setOprice(rs.getInt("oprice"));
               bVO.setInven(rs.getInt("inventory"));
               bVO.setLoc(rs.getString("loc"));
               bVO.setSales(rs.getInt("sales"));
               bVO.setProfit(rs.getInt("profit"));
            }
            else {
               bVO = new BookVO();
            }
            ev.set(bVO, mv, "수정");
            }
         catch(Exception e1) {
               JOptionPane.showMessageDialog(jf, "Exception : " + e1.toString());
         }
      }   
      else if(obj == jbtn_sel) {
         if(jtf.getText().length() != 0) {
            sql.setLength(0);
            System.out.println(sql);   //""
            sql.append("SELECT m.booknum, m.class, m.bookname, m.author"
                  + ", m.publisher, m.pdate, m.sprice"
                  + ", i.oprice, i.inventory, i.loc, i.sales, i.profit "
                  + " FROM bookmgr m, inventorytbl i ");
            sqlsetting();
            select(sql, jtf.getText());
         }
         else loading();
      }
      else if(obj == jbtn_ins) {
         ev = new EditView();
      }
      else if (obj == jlb) {
         loading();
      }
      else if(obj == jbtn_del) {
          int index[] = jtb_book.getSelectedRows();
          ArrayList<String> booknum_arr = new ArrayList<>();
          for(int i=0; i<index.length; i++) {
        	  booknum_arr.add(dtm_book.getValueAt(index[i], 0).toString());	//도서번호를 가져와서 어/리에 담음
          }
          sql.setLength(0);
          sql.append("DELETE FROM BOOKMGR WHERE BOOKNUM = ?");
          try {
        	  	con = dbMgr.getConnection();
        	  	pstmt = con.prepareStatement(sql.toString());
        	  	for(String booknum: booknum_arr) {
        	  		pstmt.setString(1, booknum);
        	  		rs = pstmt.executeQuery();
        	  	}
          } catch (SQLException e2) {
        	  System.out.println(e2.toString());
        	  JOptionPane.showMessageDialog(jf, "존재하지 않는 도서번호 입니다.");
          }
          loading();
          dbMgr.freeConnection(con, pstmt, rs);
	}

}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
//      if (e.getKeyCode()==KeyEvent.VK_ENTER){
//         sql.setLength(0);
//         sql.append("SELECT m.booknum, m.class, m.bookname, m.author"
//               + ", m.publisher, m.pdate, m.sprice"
//               + ", i.oprice, i.inventory, i.loc, i.sales, i.profit "
//               + " FROM bookmgr m, inventorytbl i ");
//         sqlsetting();
//         select(sql, jtf.getText());
//      }
		if (jtf.getText().length() != 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
			sql.setLength(0);
			sql.append("SELECT m.booknum, m.class, m.bookname, m.author" + ", m.publisher, m.pdate, m.sprice"
					+ ", i.oprice, i.inventory, i.loc, i.sales, i.profit " + " FROM bookmgr m, inventorytbl i ");
			sqlsetting();
			select(sql, jtf.getText());
		} else if (jtf.getText().length() == 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
			loading();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}