require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'a bunch of filter-sets with different properties' do
  include_context :bunch_of_filter_sets

  describe 'JSON-ROA `client` for authenticated `user`' do
    include_context :json_roa_client_for_authenticated_user do
      describe 'the filter-sets resource' do
        let :resource do
          filter_sets # force evaluation
          client.get.relation('filter-sets').get
        end

        it do
          expect(resource.response.status).to be == 200
        end
      end

    end
  end
end
