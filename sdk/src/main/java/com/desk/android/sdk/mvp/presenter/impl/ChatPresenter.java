/*
 * Copyright (c) 2015, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Salesforce.com, Inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.desk.android.sdk.mvp.presenter.impl;

import com.desk.android.sdk.Desk;
import com.desk.android.sdk.jobqueue.JobManagerProvider;
import com.desk.android.sdk.jobqueue.PostChatMessage;
import com.desk.android.sdk.mvp.model.ChatMessage;
import com.desk.android.sdk.mvp.presenter.IChatPresenter;
import com.desk.android.sdk.mvp.usecase.CreateGuestCustomer;
import com.desk.android.sdk.mvp.usecase.EndChatSession;
import com.desk.android.sdk.mvp.usecase.StartChatSession;
import com.desk.android.sdk.mvp.view.IChatView;
import com.desk.java.apiclient.model.chat.CustomerInfo;
import com.desk.java.apiclient.model.chat.SessionInfo;
import com.desk.java.apiclient.service.RxChatService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Matt Kranzler on 12/3/15.
 * Copyright (c) 2015 Desk.com. All rights reserved.
 */
public class ChatPresenter implements IChatPresenter {

    private Desk desk;
    private RxChatService chatService;
    private String chatToken;
    private CustomerInfo customerInfo;
    private SessionInfo sessionInfo;

    public interface DestroyCallback {
        void onDestroyed();
    }

    private DestroyCallback destroyCallback;
    private IChatView view;

    public ChatPresenter(DestroyCallback destroyCallback) {
        this.destroyCallback = destroyCallback;
    }

    @Override public void attach(IChatView view) {
        this.view = view;

        init(view);

        new CreateGuestCustomer(chatService, "TimmyTester", chatToken).execute()
                .flatMap(new Func1<CustomerInfo, Observable<SessionInfo>>() {
                    @Override
                    public Observable<SessionInfo> call(CustomerInfo info) {
                        customerInfo = info;
                        return new StartChatSession(chatService, customerInfo.id, chatToken, customerInfo.token).execute();
                    }
                })
                .subscribe(
                        new Action1<SessionInfo>() {
                            @Override
                            public void call(SessionInfo info) {
                                sessionInfo = info;
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                // TODO handle
                            }
                        }
                );

        final List<ChatMessage> messages = new ArrayList<>();
        boolean incoming = false;
        for (int i = 0; i < 10; i++) {
            messages.add(new ChatMessage("Message " + i, null, incoming, true));
            incoming = !incoming;
        }

        Observable.interval(1, TimeUnit.SECONDS)
                .take(messages.size())
                .map(new Func1<Long, ChatMessage>() {
                    @Override public ChatMessage call(Long aLong) {
                        return messages.get(aLong.intValue());
                    }
                })
                .map(new Func1<ChatMessage, ChatMessage>() {
                    @Override public ChatMessage call(ChatMessage chatMessage) {
                        chatMessage.setTime(new Date());
                        return chatMessage;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ChatMessage>() {
                    @Override public void call(ChatMessage chatMessage) {
                        ChatPresenter.this.view.onNewMessages(Collections.singletonList(chatMessage));
                    }
                });
    }

    @Override public void userStartedTyping() {
        // TODO notify api
    }

    @Override public void userStoppedTyping() {
        // TODO notify api
    }

    @Override public void handleNewMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message);
        JobManagerProvider.get(view.getContext()).addJob(new PostChatMessage(chatMessage, chatService, customerInfo, chatToken));
    }

    @Override public void detach(IChatView view) {
        view = null;
    }

    @Override public void destroy() {
        new EndChatSession(chatService, customerInfo.id, sessionInfo.id, chatToken, customerInfo.token)
                .execute()
                .subscribe(new Action1<Void>() {
                               @Override
                               public void call(Void aVoid) {

                               }
                           },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });

        if (destroyCallback != null) {
            destroyCallback.onDestroyed();
        }
    }

    private void init(IChatView view) {
        desk = Desk.with(view.getContext());
        chatService = desk.getRxClient().chatRx();
        chatToken = desk.getConfig().getChatToken();
    }
}
