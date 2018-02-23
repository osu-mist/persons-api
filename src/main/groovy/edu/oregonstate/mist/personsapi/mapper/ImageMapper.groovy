package edu.oregonstate.mist.personsapi.mapper

import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.Blob
import java.sql.ResultSet
import java.sql.SQLException

class ImageMapper implements ResultSetMapper<Blob> {
    public Blob map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        rs.getBlob('IMAGE')
    }
}
