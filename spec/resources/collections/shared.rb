require 'spec_helper'

shared_context :bunch_of_collections do
  let :users_count do
    5
  end

  let :collections_count do
    30
  end

  let :users do
    (1..users_count).map do
      FactoryBot.create :user
    end
  end

  let :collections do
    (1..collections_count).map do
      FactoryBot.create :collection,
                         responsible_user: users[rand(users_count)],
                         get_metadata_and_previews: (rand <= 0.8)
    end
  end
end
