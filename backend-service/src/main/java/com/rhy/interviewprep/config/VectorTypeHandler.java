package com.rhy.interviewprep.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * MyBatis TypeHandler for pgvector vector type
 * Handles conversion between float[] and PostgreSQL vector column
 * Uses string format to avoid compile-time dependency on PGobject
 */
@MappedTypes(float[].class)
@MappedJdbcTypes(JdbcType.OTHER)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
            throws SQLException {
        // Convert float[] to pgvector string format: [0.1,0.2,0.3]
        StringBuilder sb = new StringBuilder("[");
        for (int j = 0; j < parameter.length; j++) {
            if (j > 0) sb.append(",");
            sb.append(parameter[j]);
        }
        sb.append("]");
        // Use PGobject to set the vector type
        try {
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            Object pgObject = pgObjectClass.getDeclaredConstructor().newInstance();
            pgObjectClass.getMethod("setType", String.class).invoke(pgObject, "vector");
            pgObjectClass.getMethod("setValue", String.class).invoke(pgObject, sb.toString());
            ps.setObject(i, pgObject);
        } catch (Exception e) {
            throw new SQLException("Failed to create PGobject for vector", e);
        }
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return toFloatArray(value);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return toFloatArray(value);
    }

    @Override
    public float[] getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return toFloatArray(value);
    }

    private float[] toFloatArray(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // Parse pgvector format: [0.1,0.2,0.3]
        String content = value.replaceAll("[\\[\\]]", "").trim();
        if (content.isEmpty()) {
            return new float[0];
        }
        String[] parts = content.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}