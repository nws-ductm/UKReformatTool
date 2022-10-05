package com.uk.reformattool.services.filereader;

import com.uk.reformattool.common.AppConst;
import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.BasicApiInfo;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import com.uk.reformattool.common.utils.CsvUtils;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@ModuleService(flowTypes = FlowType.IMPORT_CSV, level = ModuleLevel.FILE_LEVEL)
public class CsvApiImportService extends AbstractModuleHandler {

    @Override
    @SneakyThrows(IOException.class)
    protected void execute(List<FileModel> fileModels) {
        Path csvDirectory = Paths.get(AppConfig.getInstance().getCsvDirectory());
        List<BasicApiInfo> datas = CsvUtils.readFile(csvDirectory, BasicApiInfo.class);
        List<String> contexts = datas.stream().map(BasicApiInfo::getUkFileContext).distinct()
                .collect(Collectors.toList());
        datas.forEach(BasicApiInfo::setBasicData);
        this.paramMap.put(AppConst.KEY_API_INFO, datas.stream().distinct().collect(Collectors.toList()));
        this.paramMap.put(AppConst.KEY_CONTEXT, contexts);
    }

    @Override
    public int subModuleLevel() {
        return AppConst.SUB_LEVEL_1;
    }

    @Override
    public boolean canExecute() {
        return AppConfig.getInstance().isImportAsAPI();
    }
}
