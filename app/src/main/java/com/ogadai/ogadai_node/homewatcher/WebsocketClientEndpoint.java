package com.ogadai.ogadai_node.homewatcher;

import android.util.Log;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Created by alee on 30/06/2017.
 */

@ClientEndpoint(subprotocols = {"echo-protocol"})
public class WebsocketClientEndpoint {
    private WebSocketContainer mContainer;
    private Session mUserSession = null;
    private MessageHandler mMessageHandler;

    private static final String TAG = "WebsocketClientEndpoint";

    public WebsocketClientEndpoint() {
        try {
            mContainer = ContainerProvider.getWebSocketContainer();
            mContainer.setAsyncSendTimeout(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(URI endpointURI) throws AuthenticationException, IOException, DeploymentException {
        try {
            Log.i(TAG, "connecting - " + endpointURI.toString());
            mContainer.connectToServer(this, endpointURI);
        } catch(DeploymentException depEx) {
            Throwable cause = depEx.getCause();
            Log.e(TAG, "deployment exception connecting - " + (cause != null ? cause.getMessage() : depEx.getMessage()));

            if (cause instanceof AuthenticationException) {
                throw (AuthenticationException)cause;
            } else if (cause instanceof NullPointerException) {
                throw new AuthenticationException(cause.getMessage());
            }
            throw depEx;
        } catch(Exception ex) {
            Log.e(TAG, "exception connecting - " + ex.getMessage());
            throw ex;
        }
    }

    public void disconnect() {
        try {
            if (mUserSession != null) {
                Session session = mUserSession;
                mUserSession = null;
                session.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession, EndpointConfig config) {
        Log.i(TAG, "opened");
        mUserSession = userSession;
        if (mMessageHandler != null) {
            mMessageHandler.handleOpen();
        }
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        Log.i(TAG, "closed - " + reason.getCloseCode().getCode() + " - " + reason.getReasonPhrase());
        if (mUserSession != null) {
            mUserSession = null;

            if (mMessageHandler != null) {
                mMessageHandler.handleClose(reason.getCloseCode().getCode() != 0);
            }
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (mMessageHandler != null) {
            mMessageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        mMessageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (mUserSession != null) {
            mUserSession.getAsyncRemote().sendText(message);
        }
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {
        public void handleMessage(String message);
        public void handleOpen();
        public void handleClose(boolean error);
    }
}
