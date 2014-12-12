package com.chukong.sdk.serv.view;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;

import com.chukong.sdk.Constants.Config;

/**
 * 字符串视图渲染
 * @author join
 */
public class StringView extends BaseView<String, Object[]> {

    /**
     * @details 默认charset为{@link Config#ENCODING}
     * @param args 字符串格式化参数
     * @see BaseView#render(Object, Object)
     */
    @Override
    public HttpEntity render(HttpRequest request, String content, Object[] args) throws IOException {
        if (args != null) {
            content = String.format(content, args);
        }
        return new StringEntity(content, Config.ENCODING);
    }

}
