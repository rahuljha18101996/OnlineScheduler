package dao;

import config.DBConnect;
import constants.DBConstant;
import entity.Client;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientDao {
    private static ClientDao instance = new ClientDao();
    public static ClientDao getInstance() {
        return instance;
    }
    private  DBConnect dbConnect;
    private ClientDao() {
        dbConnect = DBConnect.getInstance();
        dbConnect.connect();
    }
    public Client createClient(Client client,String password) throws Exception {
        //assert validateCredential(name,password);
        if (!validateCredential(client.ClientEmail(),password)) {
            throw new Exception("Invalid Credential");
        }

        try(PreparedStatement statement = dbConnect.getConnection().prepareStatement(String.format(
                "INSERT INTO %s (Email,Password,Name) VALUES (?, ? , ?)",
                DBConstant.CLIENT_TABLE
        ))) {
            statement.setString(1,client.ClientEmail());
            statement.setString(2,password);
            statement.setString(3, client.ClientName());
            statement.execute();

            return getClient(client.ClientEmail(), password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Client getClient(String email,String password) throws Exception {
        if (!validateCredential(email,password)) {
            throw new Exception("Invalid Credential");
        }

        try(Statement statement = dbConnect.getConnection().createStatement()) {
            String query = String.format(
                    "SELECT * FROM %s WHERE Email = '%s' AND Password = '%s';",
                    DBConstant.CLIENT_TABLE,
                    email,
                    password
            );
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                int clientId = resultSet.getInt("ClientId");
                String name = resultSet.getString("Name");
                return new Client(clientId,name,email);
            }else {
                throw new Exception("Client Not Found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateCredential(String email,String password) {
        return true;
    }
}
