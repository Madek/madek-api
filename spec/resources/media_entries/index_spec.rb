require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

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
