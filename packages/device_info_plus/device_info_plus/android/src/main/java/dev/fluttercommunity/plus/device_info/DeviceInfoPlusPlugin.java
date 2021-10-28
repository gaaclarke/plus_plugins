// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package dev.fluttercommunity.plus.device_info;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodCodec;
import io.flutter.plugin.common.StandardMethodCodec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** DeviceInfoPlusPlugin */
public class DeviceInfoPlusPlugin implements FlutterPlugin {
  static final String TAG = "DeviceInfoPlusPlugin";
  MethodChannel channel;

  @Override
  public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
    setupMethodChannel(binding.getBinaryMessenger(), binding.getApplicationContext());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    tearDownChannel();
  }

  private void setupMethodChannel(BinaryMessenger messenger, Context context) {
    String channelName = "dev.fluttercommunity.plus/device_info";
    // TODO(gaaclarke): Remove reflection guard when https://github.com/flutter/engine/pull/29147
    // becomes available on the stable branch.
    try {
      Class methodChannelClass = Class.forName("io.flutter.plugin.common.MethodChannel");
      Class taskQueueClass = Class.forName("io.flutter.plugin.common.BinaryMessenger$TaskQueue");
      Method makeBackgroundTaskQueue = messenger.getClass().getMethod("makeBackgroundTaskQueue");
      Object taskQueue = makeBackgroundTaskQueue.invoke(messenger);
      Constructor<MethodChannel> constructor =
          methodChannelClass.getConstructor(
              BinaryMessenger.class, String.class, MethodCodec.class, taskQueueClass);
      channel =
          constructor.newInstance(messenger, channelName, StandardMethodCodec.INSTANCE, taskQueue);
      Log.d(TAG, "Use TaskQueues.");
    } catch (Exception ex) {
      channel = new MethodChannel(messenger, channelName);
      Log.d(TAG, "Don't use TaskQueues.");
    }
    final MethodCallHandlerImpl handler =
        new MethodCallHandlerImpl(context.getContentResolver(), context.getPackageManager());
    channel.setMethodCallHandler(handler);
  }

  private void tearDownChannel() {
    channel.setMethodCallHandler(null);
    channel = null;
  }
}
