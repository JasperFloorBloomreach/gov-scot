const webpack = require('webpack');
const path = require('path');
const aliasPath = '../../../../../site/src/main/webapp/assets/scripts/vendor/';

// Packs JS files & deps into bundles
module.exports = {
    devtool: 'source-map',

    entry: {
        'default': './src/scripts/govscot/format.default.js'
    },

    resolve: {
        modules: [
            './src/main/resources/site/assets/scripts',
            'node_modules'
        ],

        extensions: ['', '.js'],

        // equivalent to requirejs paths
        alias: {
            'jquery': aliasPath + 'jquery.min',
            'jquery.dotdotdot': aliasPath + 'jquery.dotdotdot.min',
            'moment': aliasPath + 'moment'
        }
    },

    resolveLoader: {
        alias: {

        }
    },

    output: {
        path: path.resolve(__dirname, 'src/main/resources/site/assets/scripts'),
        filename: '[name].js'
    },

    module: {
        loaders: [
            {
                test: /(govscot|shared|utils)\/.*\.js$/,
                exclude: /(node_modules|bower_components)/,
                loader: 'babel',
                query: {
                    presets: ['es2015']
                }
            }
        ]
    },


    development: {
        plugins: [
            new webpack.DefinePlugin({
                DEBUG: true,
                PRODUCTION: false
            })
        ]
    },

    production: {
        plugins: [
            new webpack.DefinePlugin({
                DEBUG: false,
                PRODUCTION: true
            }),
            new webpack.optimize.DedupePlugin(),
            new webpack.optimize.UglifyJsPlugin()
        ]
    }

};