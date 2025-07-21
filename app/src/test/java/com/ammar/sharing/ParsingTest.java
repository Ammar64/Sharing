package com.ammar.sharing;

import com.ammar.sharing.network.Request;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ParsingTest {
    @Test
    public void testRequestParamsDecode() {
        String example = "filename=file_name_test123";
        Request request = new Request();
        Method method;
        try {
            method = Request.class.getDeclaredMethod("decodeParams", String.class);
            method.setAccessible(true);
            method.invoke(request, example);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals("file_name_test123", request.getParam("filename"));
        method.setAccessible(true);

    }
}
