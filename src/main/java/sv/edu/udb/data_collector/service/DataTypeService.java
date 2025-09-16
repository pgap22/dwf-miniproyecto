package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.domain.DataType;

import java.util.List;


public interface DataTypeService {

    public List<DataType> listPrimitives();

    public List<DataType> listAll();

    public DataType getById(String id);
}