/*
 * Copyright (C) 2022 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer.linkcheck;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class LinkcheckerAvailabilityStatusCheckerStatusWriter implements Consumer<Writer> {

    private final static Logger logger = LoggerFactory.getLogger(LinkcheckerAvailabilityStatusCheckerStatusWriter.class);
    private final DataSource dataSource;

    public LinkcheckerAvailabilityStatusCheckerStatusWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void accept(Writer writer) {
        try {
            writeStatus(writer);
            writer.flush();
        } catch (IOException ex) {
            logger.error("Cannot write status for linkchecker database connection", ex);
        }
    }

    private void writeStatus(Writer writer) throws IOException {
        try {
            if (dataSource instanceof HikariDataSource) {
                writeHikariPoolStatus(writer, ((HikariDataSource) dataSource));
            } else if (dataSource != null) {
                writeDataSourceStatus(writer);
            } else {
                writer.write("DataSource object is null!");
            }
        } catch (SQLException ex) {
            logger.error("Cannot get status for linkchecker database connection", ex);
            writer.write(ex.getMessage());
        }
    }

    private void writeHikariPoolStatus(Writer writer, final HikariDataSource ds) throws IOException {
        final HikariPoolMXBean hikariPoolMXBean = ds.getHikariPoolMXBean();
        writer.write(String.format("HikariDataSource <active connections: %d, idle connections: %d, threads awaiting connection: %d, total connections: %d>",
                hikariPoolMXBean.getActiveConnections(),
                hikariPoolMXBean.getIdleConnections(),
                hikariPoolMXBean.getThreadsAwaitingConnection(), hikariPoolMXBean.getTotalConnections()));
    }

    private void writeDataSourceStatus(Writer writer) throws IOException, SQLException {
        final Connection connection = dataSource.getConnection();
        if (connection == null) {
            writer.write(String.format("DataSource: <%s> - connection object is NULL!!", dataSource));
        } else {
            writer.write(String.format("DataSource: <%s>, connection closed=<%b>", dataSource, connection.isClosed()));
        }
    }

}
