package com.wfy.annotation.model;

import javax.lang.model.element.Element;

public class RouterBean {


    public enum Type {
        ACTIVITY
    }

    private Type type;
    //类的节点
    private Element element;
    //被@ARouter注解的Class对象
    private Class<?> clzz;
    //路由的组名
    private String group;
    //路由的地址
    private String path;


    private RouterBean(Builder builder) {
        this.path = builder.path;
        this.group = builder.group;
        this.element = builder.element;
    }


    private RouterBean(Type type, Class<?> clzz, String path, String group) {
        this.type = type;
        this.clzz = clzz;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(Type type, Class<?> clzz, String path, String group) {
        return new RouterBean(type, clzz, path, group);
    }


    public void setType(Type type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getClzz() {
        return clzz;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public static final class Builder {
        //类的节点
        private Element element;
        //路由的组名
        private String group;
        //路由的地址
        private String path;

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public RouterBean build() {
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path必填项为空，如：/app/MainActivity");
            }

            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
