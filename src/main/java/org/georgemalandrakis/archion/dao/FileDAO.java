package org.georgemalandrakis.archion.dao;

import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.exception.FileDBException;
import org.georgemalandrakis.archion.mapper.UserFileMapper;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.core.ArchionRequest;
import org.joda.time.Instant;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class FileDAO extends AbstractDAO {

    public FileDAO(ConnectionManager connectionObject) {
        super(connectionObject);
    }


    public boolean file_exists(String user_id, String sha1_hash) {

        String sql = "SELECT * from  file_metadata_table ou WHERE  sha1_digest = ? AND associated_user = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, sha1_hash);
            statement.setObject(2, java.util.UUID.fromString(user_id));

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            closeSilently(resultSet, statement);
        }
        return false;

    }


    public FileMetadata create(FileMetadata fileMetadata) throws FileDBException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Integer results;
        try {
            String newLocalFilename = (fileMetadata.getLocalfilename() != null ? fileMetadata.getLocalfilename() : fileMetadata.getFileid());
            fileMetadata.setLocalfilename(newLocalFilename);

            statement = connection.prepareStatement("INSERT INTO file_metadata_table (id, associated_user, original_filename, file_extension, " +
                    "size_in_kilobytes, date_created, file_scope, procedure_phase, sha1_digest) VALUES" +
                    " (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setObject(1, java.util.UUID.fromString(fileMetadata.getFileid()));
            statement.setObject(2, java.util.UUID.fromString(fileMetadata.getUserid()));
            //error if null!
            statement.setString(3, fileMetadata.getFilename());
            statement.setString(4, fileMetadata.getFileextension());
            statement.setInt(5, Integer.valueOf(fileMetadata.getSizeinkbs()));
            statement.setTimestamp(6, fileMetadata.getCreated());
            statement.setString(7, fileMetadata.getFiletype());
            statement.setString(8, fileMetadata.getPhase().toString());
            statement.setString(9, fileMetadata.getSha1Hash());

            results = statement.executeUpdate();

            if (results < 0) {
                throw new FileDBException(fileMetadata);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            throw new FileDBException(fileMetadata);

        } finally {
            closeSilently(resultSet, statement);
        }
        return fileMetadata;
    }

    public FileMetadata retrieve(String id) {
        FileMetadata userile = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement("SELECT  * FROM file_metadata_table WHERE id = ?");
            statement.setObject(1, java.util.UUID.fromString(id));
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userile = UserFileMapper.map(resultSet);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        } finally {
            closeSilently(resultSet, statement);
        }

        return userile;

    }

    public void updateLastAccessed(String fileid) {
        //TODO: Implement
    }


    public List<FileMetadata> retrieveList(String id) {
        List<FileMetadata> fileList = new ArrayList<>();
        FileMetadata usf;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement("SELECT  * FROM file_metadata_table WHERE associated_user = ?");

            statement.setObject(1, UUID.fromString(id));
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                usf = UserFileMapper.map(resultSet);
                fileList.add(usf);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            closeSilently(resultSet, statement);
        }

        return fileList;

    }


    public List<FileMetadata> fetchOldFiles(String filetype) {
        List<FileMetadata> fileList = new ArrayList<>();
        FileMetadata usf;
        Calendar calendar = java.util.Calendar.getInstance();
        if (filetype.equalsIgnoreCase(ArchionConstants.FILES_TEMP_FILETYPE)) {
            calendar.add(Calendar.MONTH, -1);

        } else if (filetype.equalsIgnoreCase(ArchionConstants.FILES_TEST_FILETYPE)) {
            calendar.add(Calendar.MINUTE, -5);
        } else if (filetype.equalsIgnoreCase(ArchionConstants.FILES_DEFAULT_FILETYPE)) {
            calendar.add(Calendar.YEAR, -1); //One year old files are regarded as old
        } else {
            return null;
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT  * FROM file_metadata_table WHERE file_scope = ? AND last_accessed < ?");
            statement.setString(1, filetype);
            statement.setTimestamp(2, new Timestamp(calendar.getTimeInMillis()));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                usf = UserFileMapper.map(resultSet);
                fileList.add(usf);
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        } finally {
            closeSilently(resultSet, statement);
        }

        return fileList;
    }

    public List<FileMetadata> fetchUserDuplicates() {
        List<FileMetadata> files = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String sql = "SELECT * from  file_metadata_table ou WHERE (SELECT count(*) from file_metadata_table inr " +
                    "where inr.sha1_digest = ou.sha1_digest AND inr.associated_user = ou.associated_user) > 1 ";

            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                files.add(UserFileMapper.map(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            closeSilently(resultSet, statement);
        }

        return files;
    }

    public List<FileMetadata> fetchNotAccessedForThreeDays() {
        List<FileMetadata> files = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String sql = "SELECT * from file_metadata_table WHERE last_accessed <= ? ;";

            statement = connection.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                files.add(UserFileMapper.map(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            closeSilently(resultSet, statement);
        }

        return files;
    }

    public FileMetadata update(FileMetadata fileMetadata) throws FileDBException {
        PreparedStatement statement = null;

        Integer count = 0;
        try {
            statement = connection.prepareStatement("UPDATE file_metadata_table SET date_created = ?, last_accessed = ?, last_modified = ?, file_scope = " +
                    "?, procedure_phase = ? " +
                    "  WHERE id = ?");
            statement.setTimestamp(1, fileMetadata.getCreated());
            statement.setTimestamp(2, fileMetadata.getLastAccessed());
            statement.setTimestamp(3, fileMetadata.getLastmodified());
            statement.setString(4, fileMetadata.getFiletype());
            statement.setString(5, fileMetadata.getPhase().toString());
            statement.setObject(6, java.util.UUID.fromString(fileMetadata.getFileid()));

            count = statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            throw new FileDBException(fileMetadata);
        } finally {
            closeSilently(statement);
        }

        if (count > 0) {
            return this.retrieve(fileMetadata.getFileid());
        }
        return null;
    }


    public Integer deleteFileById(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM file_metadata_table WHERE id = ?");
        statement.setObject(1, java.util.UUID.fromString(id));
        return statement.executeUpdate();
    }


}
