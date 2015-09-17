require 'active_support/all'
ENV['RAILS_ENV'] = ENV['RAILS_ENV'].presence || 'test'

require 'faker'
require 'config/bundle'
require 'config/rails'
require 'config/database'
require 'config/web'

require 'shared/clients'

require 'pry'
