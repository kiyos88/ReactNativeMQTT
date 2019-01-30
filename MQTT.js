import {DeviceEventEmitter, NativeModules} from 'react-native';

export default {
    init: (conf) => NativeModules.RNMQTT.init(conf),
    connect: () => NativeModules.RNMQTT.connect(),
    subscribeToTopic: (topic) => NativeModules.RNMQTT.subscribeToTopic(topic),
    publish: (message) => NativeModules.RNMQTT.publish(message),
    onDisconnect: (callback) => DeviceEventEmitter.addListener('disconnect', callback),
    onReconnect: (callback) => DeviceEventEmitter.addListener('reconnect', callback),
    onMessage: (callback) => DeviceEventEmitter.addListener('message_received', callback),
}