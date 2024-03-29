package cqut.icode.common.shiro.realm;


import cqut.icode.common.enums.StatusEnums;
import cqut.icode.system.entity.User;
import cqut.icode.system.entity.perm.Perm;
import cqut.icode.system.entity.perm.Role;
import cqut.icode.system.service.PermService;
import cqut.icode.system.service.RoleService;
import cqut.icode.system.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tq
 */
@Component
public class AuthRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermService menuService;

    /**
     * 权限认证
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        /**
         * 1. 获取当前登录用户信息
         * 2. 获取当前用户关联的角色、权限等
         * 3. 将角色、权限封装到AuthorizationInfo对象中并返回
         */
        User user = (User) SecurityUtils.getSubject().getPrincipal();

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        // 获取用户角色
        List<Role> roleList = roleService.findUserRole(user.getUsername());
        Set<String> roleSet = roleList.stream().map(Role::getName).collect(Collectors.toSet());
        System.out.println(roleSet);
        simpleAuthorizationInfo.setRoles(roleSet);

        // 获取用户权限
        List<Perm> menuList = menuService.findUserPermissions(user.getUsername());
        Set<String> permSet = menuList.stream().map(Perm::getPerms).collect(Collectors.toSet());
        System.out.println(permSet);
        simpleAuthorizationInfo.setStringPermissions(permSet);

        return simpleAuthorizationInfo;
    }

    /**
     * 身份校验
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        /**
         * 1. 从token中获取输入的用户名
         * 2. 通过username查询数据库得到用户对象
         * 3. 调用Authenticator进行密码校验
         */
        // 获取用户名和密码
        String username = (String) authenticationToken.getPrincipal();

        User user = userService.findByName(username);

        if (user == null) {
            throw new UnknownAccountException(String.valueOf(StatusEnums.ACCOUNT_UNKNOWN.getInfo()));
        }
        /**
         * 交给Shiro进行密码的解密校验
         * 调用SecurityUtils.getSubject().getPrincipal() 遇到类型转换问题，报错 String !=> User
         * 请参考这篇文章：{@link https://blog.csdn.net/qq_35981283/article/details/78634575}
         */
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user,
                user.getPassword(),
                ByteSource.Util.bytes(user.getSalt()),
                getName()
        );
        return authenticationInfo;
    }
}
