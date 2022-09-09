package com.flycode.elevatormanager.dtos;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.util.Map;

public class CustomClassMapper implements ClassMapper {

    public String getClassIdFieldName() {
        return "__TypeId__";
    }

    @Override
    public void fromClass(Class<?> aClass, MessageProperties messageProperties) {
        messageProperties.getHeaders().put(this.getClassIdFieldName(), aClass.getPackageName());
    }

    @Override
    public Class<?> toClass(MessageProperties messageProperties) {
        Map<String, Object> headers = messageProperties.getHeaders();
        Object classIdFieldNameValue = headers.get(this.getClassIdFieldName());
        String classId = null;
        if (classIdFieldNameValue != null) {
            classId = classIdFieldNameValue.toString();
        }

        if (classId == null) {
            throw new MessageConversionException("failed to convert Message content. Could not resolve " + this.getClassIdFieldName() + " in header");
        } else {
            if (classId.contains("Response")) {
                return Response.class;
            } else if (classId.contains("Task")) {
                return Task.class;
            } else {
                throw new MessageConversionException("failed to convert Message content. Could not resolve " + this.getClassIdFieldName() + " in header. Unknown class.");
            }
        }
    }
}
