package ru.tstu.telegram.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import ru.tstu.telegram.model.TelegramMessage;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Created by user on 15.11.17.
 */
@Repository
public class MessagesDAOService extends JdbcDaoSupport {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public void saveMassage(TelegramMessage message) {
        getJdbcTemplate().update("INSERT INTO MESSAGES (USER_ID, MESSAGE) VALUES (?, ?)",
                message.getUserId(), message.getText());
    }

    public void getLastTask(SendMessage response){
        SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_text FROM all_tasks ORDER BY task_id DESC LIMIT 1");
        drs.last();
        response.setText(drs.getString("task_name") + "\n" + drs.getString("task_text"));
    }

    public void getAllTasks(SendMessage response){
        SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_description FROM all_tasks ORDER BY task_id ASC");
        String msg = "";
        while(drs.next()){
            msg += drs.getString("task_id") + ": "  + drs.getString("task_name")
                    + "\n" + drs.getString("task_description") + "\n";
        }
        response.setText(msg);
    }

    public void getChoseTask(SendMessage response, String text) {
        SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_text FROM all_tasks WHERE task_id = " + text);
        drs.next();
        response.setText(drs.getString("task_name") + "\n" + drs.getString("task_text"));
    }

    public void getCurrentTask(SendMessage response, Integer userId){
        SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_text FROM all_tasks WHERE task_id = " +
                "(SELECT global_task_id FROM current_task WHERE user_id =" + userId + ")");
        drs.next();
        response.setText(drs.getString("task_name") + "\n" + drs.getString("task_text"));
    }

    public void getFollowTasks(SendMessage response, Integer userId){
        SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_description FROM all_tasks WHERE task_id = " +
                "(SELECT global_task_id FROM follow_tasks WHERE user_id = " + userId + ")");

        String msg = "";
        while(drs.next()){
            msg += drs.getString("task_id") + ": "  + drs.getString("task_name")
                    + "\n" + drs.getString("task_description") + "\n";
        }
        response.setText(msg);
    }
    /*SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT global_task_id FROM follow_tasks WHERE user_id =" + userId);
        String msg = "";
        while(drs.next()){
        SqlRowSet drs2 = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_description FROM all_tasks WHERE task_id = " +
                drs.getString("global_task_id"));
            msg += drs2.getString("task_id") + ": "  + drs2.getString("task_name")
                    + "\n" + drs2.getString("task_description") + "\n";
        }
        response.setText(msg);*/

    /*
    SqlRowSet drs = getJdbcTemplate().queryForRowSet("SELECT task_id,task_name,task_description FROM all_tasks WHERE task_id = " +
                "(SELECT global_task_id FROM follow_tasks WHERE user_id =" + userId + ")");

        String msg = "";
        while(drs.next()){
            msg += drs.getString("task_id") + ": "  + drs.getString("task_name")
                    + "\n" + drs.getString("task_description") + "\n";
        }
        response.setText(msg);
        */
}
