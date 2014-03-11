package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.*;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public class PostgreSqlDataStructureDaoImpl extends BasicDataStructureDaoImpl {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlDataStructureDaoImpl.class);

    @Override
    protected BasicQueryHelper createQueryHelper() {
        return new PostgreSqlQueryHelper();
    }

    @Override
    protected String generateDoesTableExistQuery() {
        return "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";
    }

    @Override
    protected String generateCountTablesQuery() {
        return "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
    }
}
