require 'spec_helper'

shared_context :bunch_of_media_entries do
  let :users_count do
    5
  end

  let :media_entries_count do
    30
  end

  let :users do
    (1..users_count).map do
      FactoryGirl.create :user
    end
  end

  let :media_entries do
    (1..media_entries_count).map do
      FactoryGirl.create :media_entry,
                         responsible_user: users[rand(users_count)],
                         is_published: (rand <= 0.9),
                         get_metadata_and_previews: (rand <= 0.8),
                         get_full_size: (rand <= 0.3)
    end
  end
end

describe 'a bunch of media entries with different properties' do
  include_context :bunch_of_media_entries

  describe 'JSON-ROA `client` for authenticated `user`' do
    include_context :json_roa_client_for_authenticated_user do
      describe 'the media_entries resource' do
        let :resource do
          media_entries # force evaluation
          client.get.relation('media-entries').get
        end

        it do
          expect(resource.response.status).to be == 200
        end
      end
    end
  end
end
