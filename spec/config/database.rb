require 'pg_tasks'

RSpec.configure do |config|
  config.before(:each) do
    PgTasks.truncate_tables
    PgTasks.data_restore Rails.root.join('datalayer', 'db', 'seeds.pgbin')
  end
end

def with_disabled_triggers
  ActiveRecord::Base.connection.execute 'SET session_replication_role = REPLICA;'
  result = yield
  ActiveRecord::Base.connection.execute 'SET session_replication_role = DEFAULT;'
  result
end
