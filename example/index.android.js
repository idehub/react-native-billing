/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';
import React, {
  AppRegistry,
  Component,
  StyleSheet,
  Text,
  View
} from 'react-native';

const InAppBilling = require('react-native-billing');

class example extends Component {
  constructor(props) {
    super(props);
    this.state = {
      detailsText: "Purchasing test product"
    }
  }

  componentDidMount() {
    InAppBilling.open().
    then(() => InAppBilling.purchase('android.test.purchased'))
    .then((details) => {
      console.log(details);
      this.setState({
        detailsText: details.productId
      })
      return InAppBilling.getProductDetails('android.test.purchased');
    })
    .then(InAppBilling.close).catch((error) => {
      console.log(error);
    });
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          InApp Billing sample:
        </Text>
        <Text style={styles.instructions}>
          {this.state.detailsText}
        </Text>
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

AppRegistry.registerComponent('example', () => example);
