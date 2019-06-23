'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_URL: '"http://localhost:9090/apartmentManager"',
  FILE_MANAGER_URL: '"http://localhost:9090/fileManager"'
})
