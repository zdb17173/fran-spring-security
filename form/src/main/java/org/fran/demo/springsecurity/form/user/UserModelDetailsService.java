package org.fran.demo.springsecurity.form.user;

import org.fran.demo.springsecurity.form.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class UserModelDetailsService implements UserDetailsService {

   private final Logger log = LoggerFactory.getLogger(UserModelDetailsService.class);

   @Override
   public UserDetails loadUserByUsername(final String username) {
      log.debug("Authenticating user '{}'", username);

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_logined"));

      if(username.equals("ed")){
         authorities.add(new SimpleGrantedAuthority("ROLE_editor"));

         CustomUser ed = new CustomUser(
                 "1",
                 "ed",//user name
                 SecurityUtils.encryptPassword((String) "1"),//user pwd
                 authorities);
         return ed;
      }
      if(username.equals("fed")){
         authorities.add(new SimpleGrantedAuthority("ROLE_copyEditor"));

         CustomUser ed = new CustomUser(
                 "2",
                 "fed",//user name
                 SecurityUtils.encryptPassword((String) "1"),//user pwd
                 authorities);
         return ed;
      }
      if(username.equals("ced")){
         authorities.add(new SimpleGrantedAuthority("ROLE_chiefEditor"));
         authorities.add(new SimpleGrantedAuthority("ROLE_chief"));

         CustomUser ed = new CustomUser(
                 "3",
                 "ced",//user name
                 SecurityUtils.encryptPassword((String) "1"),//user pwd
                 authorities);
         return ed;
      }
      if(username.equals("manager")){
         authorities.add(new SimpleGrantedAuthority("ROLE_publishNewsManager"));

         CustomUser ed = new CustomUser(
                 "4",
                 "manager",//user name
                 SecurityUtils.encryptPassword((String) "1"),//user pwd
                 authorities);
         return ed;
      }

      return new CustomUser(
              username,
              username,//user name
              SecurityUtils.encryptPassword((String) username),//user pwd
              authorities);
   }

   public static class CustomUser implements UserDetails {
      String uid;
      String userName;
      String passWord;
      List<GrantedAuthority> grantedAuthority;

      public CustomUser(
              String uid,
              String userName,
              String passWord,
              List<GrantedAuthority> grantedAuthority
      ) {
         this.uid = uid;
         this.userName = userName;
         this.passWord = passWord;
         this.grantedAuthority = grantedAuthority;
      }

      public String getUid() {
         return uid;
      }

      public void setUid(String uid) {
         this.uid = uid;
      }

      public void setPassWord(String passWord){
         this.passWord = passWord;
      }

      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
         return grantedAuthority;
      }

      @Override
      public String getPassword() {
         return passWord;
      }

      @Override
      public String getUsername() {
         return userName;
      }

      @Override
      public boolean isAccountNonExpired() {
         return true;
      }

      @Override
      public boolean isAccountNonLocked() {
         return true;
      }

      @Override
      public boolean isCredentialsNonExpired() {
         return true;
      }

      @Override
      public boolean isEnabled() {
         return true;
      }
   }
}