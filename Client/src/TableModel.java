import javax.swing.table.DefaultTableModel;

public class TableModel extends DefaultTableModel {
    TableModel(String[] object, int i){
        super(object,i);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
