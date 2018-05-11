/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifpe.recife.avalon.bean;

import br.edu.ifpe.recife.avalon.model.usuario.GrupoEnum;
import br.edu.ifpe.recife.avalon.model.usuario.Usuario;
import br.edu.ifpe.recife.avalon.servico.UsuarioServico;
import br.edu.ifpe.recife.avalon.util.AvalonUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 *
 * @author eduardo.f.amaral
 */
@Named(value  = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final String NAV_HOME_PROFESSOR = "goHomeProfessor";
    private static final String NAV_HOME_ALUNO = "goHomeAluno";
    private static final String LOGIN_FALHA_GERAL = "login.falha.geral";
    private static final String LOGIN_FALHA_TOKEN = "login.falha.token";
    private static final String IFPE_DOMINIO = "recife.ifpe.edu.br";

    @EJB
    private UsuarioServico usuarioServico;

    @Valid
    private Usuario usuario = new Usuario();

    @ManagedProperty("#{param.token}")
    private String token;

    private FacesContext contexto = FacesContext.getCurrentInstance();

    private Payload payload;

    /**
     * Método para realizar login via conta Google.
     */
    public void googleLogin() {
        
        Map<String, String> param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        token = param.get("token");
        
        if (token != null) {

            verificarToken();

            if (isNotUsuarioCadastrado()) {
                salvarNovoUsuario();
            }

            realizarLogin();

        }else{
            contexto.addMessage(null, new FacesMessage(AvalonUtil.getInstance().getMensagem(LOGIN_FALHA_GERAL)));
        }

    }

    /**
     * Método para verificar o token enviado.
     */
    private void verificarToken() {
        GoogleIdTokenVerifier verificador = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("131562098478-bvjjnubvmsauka865rsd8rrdol9flj9n.apps.googleusercontent.com"))
                .build();

        payload = null;

        try {

            GoogleIdToken googleIdToken = verificador.verify(token);

            if (googleIdToken != null) {
                payload = googleIdToken.getPayload();

                usuario.setEmail(payload.getEmail());
            } else {
                contexto.addMessage(null, new FacesMessage(AvalonUtil.getInstance().getMensagem(LOGIN_FALHA_TOKEN)));
            }

        } catch (IOException | GeneralSecurityException e) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, e);
            contexto.addMessage(null, new FacesMessage(AvalonUtil.getInstance().getMensagem(LOGIN_FALHA_GERAL)));
        }

    }

    /**
     * Método para salvar um novo usuário.
     */
    private void salvarNovoUsuario() {
        if (payload != null) {

            usuario.setNome(payload.get("given_name").toString());
            usuario.setSobrenome(payload.get("family_name").toString());
            
            if(payload.getHostedDomain() != null && payload.getHostedDomain().contains(IFPE_DOMINIO)){
                usuario.setGrupo(GrupoEnum.PROFESSOR);
            }else{
                usuario.setGrupo(GrupoEnum.ALUNO);
            }
                    
            usuarioServico.salvar(usuario);
            
        }
    }

    /**
     * Método para verificar se o usuário já existe.
     *
     * @return boolean
     */
    private boolean isNotUsuarioCadastrado() {
        Usuario usuarioAplicacao = usuarioServico.buscarUsuarioPorEmail(payload.getEmail());
        return usuarioAplicacao == null;
    }

    /**
     * Método para realizar Login.
     */
    private void realizarLogin() {
        usuario = usuarioServico.buscarUsuarioPorEmail(usuario.getEmail());

        if (usuario != null) {
            contexto.getExternalContext().getSessionMap().put("usuario", usuario);
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            
            try {
                request.login(usuario.getEmail(), usuario.getEmail());
            } catch (ServletException ex) {
                contexto.addMessage(null, new FacesMessage(AvalonUtil.getInstance().getMensagem(LOGIN_FALHA_GERAL)));
                Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            NavigationHandler navigationHandler = contexto.getApplication().getNavigationHandler();
            
            if(GrupoEnum.PROFESSOR.equals(usuario.getGrupo())){
                navigationHandler.handleNavigation(contexto, null, NAV_HOME_PROFESSOR);
            }else{
                navigationHandler.handleNavigation(contexto, null, NAV_HOME_ALUNO);
            }
            
        } else {
            contexto.addMessage(null, new FacesMessage(AvalonUtil.getInstance().getMensagem(LOGIN_FALHA_GERAL)));
        }

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
