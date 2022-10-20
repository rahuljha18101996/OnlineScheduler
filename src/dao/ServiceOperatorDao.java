package dao;

import config.DBConnect;
import constants.DBConstant;
import entity.ServiceOperator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceOperatorDao {
    private static ServiceOperatorDao instance = new ServiceOperatorDao();
    public static ServiceOperatorDao getInstance() {
        return instance;
    }
    private DBConnect dbConnect;
    private ServiceOperatorDao() {
        dbConnect = DBConnect.getInstance();
        dbConnect.connect();
    }

    public List<ServiceOperator> getServiceOperators() throws Exception {
        List<ServiceOperator> serviceOperators = new ArrayList<>();
        try(Statement statement = dbConnect.getConnection().createStatement()) {
            String query = String.format(
                    "SELECT * FROM %s;",
                    DBConstant.OPERATOR_TABLE
            );
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int operatorId = resultSet.getInt("OperatorId");
                String operatorName = resultSet.getString("OperatorName");
                serviceOperators.add(new ServiceOperator(operatorId,operatorName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return serviceOperators;
    }
}
