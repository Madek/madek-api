require 'pg_tasks'

def clean_db
  PgTasks.truncate_tables
end

RSpec.configure do |config|
  config.before(:each) do
    clean_db
  end
end

def with_disabled_triggers
  ActiveRecord::Base.connection.execute 'SET session_replication_role = REPLICA;'
  result = yield
  ActiveRecord::Base.connection.execute 'SET session_replication_role = DEFAULT;'
  result
end
