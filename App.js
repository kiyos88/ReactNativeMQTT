/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 * @lint-ignore-every XPLATJSCOPYRIGHT1
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View} from 'react-native';
import MQTT from "./MQTT";

const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
    android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {

    componentDidMount() {
        MQTT.init({
            protocol: "tcp",
            host: "192.168.0.39",
            port: 1883,
            user: "",
            pass: "",
            clean: false,
            automaticReconnect: true,
            clientId: "test-123",
            qos: 1
        });

        MQTT.connect().then(message => alert("client " + message + " successfully connected")).catch(err => alert(err));

        MQTT.subscribeToTopic("games/${id}/questions");

        MQTT.onMessage(message => alert(JSON.stringify(message)));

        MQTT.publish({
            topic: "Test",
            payload: "{'Test': 'Test123'}",
            qos: 1,
            retain: false
        });


    }

    render() {
        return (
            <View style={styles.container}>
                <Text style={styles.welcome}>Welcome to React Native!</Text>
                <Text style={styles.instructions}>To get started, edit App.js</Text>
                <Text style={styles.instructions}>{instructions}</Text>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});
