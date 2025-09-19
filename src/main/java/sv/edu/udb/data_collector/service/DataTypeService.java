package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.response.DataTypeResponse;

import java.util.List;

public interface DataTypeService {

    List<DataTypeResponse> listPrimitives();

    List<DataTypeResponse> listAll();

    DataTypeResponse getById(String id);
}