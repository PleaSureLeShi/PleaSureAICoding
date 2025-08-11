package com.pleasure.pleasureaicoding.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.pleasure.pleasureaicoding.model.dto.app.AppAddRequest;
import com.pleasure.pleasureaicoding.model.dto.app.AppQueryRequest;
import com.pleasure.pleasureaicoding.model.entity.App;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/PleaSureLeShi">PleaSure乐事</a>
 */
public interface AppService extends IService<App> {
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    String deployApp(Long appId, User loginUser);

    AppVO getAppVo(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
