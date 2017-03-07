// karma.conf.js
var webpack = require('webpack');
var path = require('path');

module.exports = function (config) {
  config.set({
    browsers: ['PhantomJS'],
    singleRun: true,
    frameworks: ['mocha'],
    files: [
      'test/test_index.js'
    ],
    preprocessors: {
      'test/test_index.js': ['webpack']
    },
    reporters: ['mocha'],
    webpackServer: {
      noInfo: true
    },
    webpack: {
      plugins: [
        new webpack.IgnorePlugin(/react\/lib\/ReactContext/),
        new webpack.IgnorePlugin(/react\/lib\/ExecutionEnvironment/)
      ],
      module: {
        noParse: [
           /\/sinon\.js/,
        ],
        loaders: [
          {
            test: /\.js$/,
            exclude: /node_modules/,
            include: __dirname,
            loader: 'babel',
          },
          { test: /\.json$/, loader: "json" },
          {
            test: /\.less$/,
            loader: "style-loader!css-loader!less-loader"
          }
        ]
      },
      resolve: {
        alias: {
          config: path.join(__dirname, 'config', 'test'),
          sinon: 'sinon/pkg/sinon.js'
        }
      },
      watch: true
    }
  });
};
