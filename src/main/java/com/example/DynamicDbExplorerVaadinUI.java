package com.example;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.LazyDynaClass;
import org.apache.commons.beanutils.LazyDynaMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.viritin.fields.MTable;

@SpringUI
@Theme("valo")
public class DynamicDbExplorerVaadinUI extends UI {

    final HorizontalSplitPanel layout = new HorizontalSplitPanel();

    @Autowired
    private JdbcTemplate template;

    @Override
    protected void init(VaadinRequest request) {
        setContent(layout);
        layout.setSplitPosition(30, Unit.PERCENTAGE);
        layout.setFirstComponent(createTableList());
    }

    private Table createTableList() {
        MTable<String> tableListing = new MTable<>(String.class)
                .withFullHeight()
                .withFullWidth()
                .withGeneratedColumn("Table", s -> s)
                .withProperties("Table");

        try (Connection cnn = template.getDataSource().getConnection()) {
            ResultSet rs = cnn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tableListing.addBeans(rs.getString(3));
            }
        } catch (SQLException e) {
            //ignoring the error
        }

        tableListing.addMValueChangeListener(s -> displayTableData(s.getValue()));
        return tableListing;
    }

    private DynaClass getDynaClass(String tableName) {
        String sql = "select * from " + tableName + " where 1 = 0";
        final LazyDynaClass clazz = new LazyDynaClass(tableName);
        template.query(sql, (ResultSet rs) -> {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 0; i < md.getColumnCount(); i++) {
                clazz.add(md.getColumnName(i + 1));
            }
            return null;
        });
        return clazz;
    }

    private void displayTableData(String tableName) {
        List<LazyDynaBean> data = template
                .queryForList("select * from " + tableName)
                .stream()
                .map(map -> new LazyDynaMap(map))
                .collect(Collectors.toList());
        
        layout.setSecondComponent(
                new MTable(getDynaClass(tableName))
                .addBeans(data)
                .withFullHeight()
                .withFullWidth()
        );
    }

}
