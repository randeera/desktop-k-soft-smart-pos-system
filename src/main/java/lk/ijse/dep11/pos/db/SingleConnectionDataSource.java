package lk.ijse.dep11.pos.db;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SingleConnectionDataSource {
    private static SingleConnectionDataSource instance;
    private final Connection connection;

    private SingleConnectionDataSource() throws IOException {
        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("/view/application.properties"));
            String url=properties.getProperty("app.datasource.url");
            String userName=properties.getProperty("app.datasource.username");
            String password=properties.getProperty("app.datasource.password");
            connection= DriverManager.getConnection(url, userName,password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static SingleConnectionDataSource getInstance() throws IOException {
        return (instance== null) ? (instance =new SingleConnectionDataSource() ): instance;
    }
    private void generateSchema() throws Exception {
        URL url = getClass().getResource("/schema.sql");
        Path path = Paths.get(url.toURI());
        String dbScript = Files.readAllLines(path).stream()
                .reduce((prevLine, currentLine) -> prevLine + currentLine).get();
        connection.createStatement().execute(dbScript);
    }

    public Connection getConnection() {
        return connection;
    }
}
