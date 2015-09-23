require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'ordering media entries by created_at' do
  include_context :bunch_of_media_entries

  include_context :json_roa_client_for_authenticated_user do
    let :media_entries_relation do
      media_entries # force evaluation
      client.get.relation('media-entries')
    end

    def resource(order = nil)
      media_entries_relation.get('order' => order)
    end

    def media_entries_created_at(order = nil)
      # to_datetime.strftime('%Q').to_i => int with ms precision
      resource(order)
        .data['media-entries']
        .map { |me| MediaEntry.unscoped.find(me['id']) }
        .map { |me| me.created_at.to_datetime.strftime('%Q').to_i }
    end

    it 'defaults to :asc' do
      media_entries_created_at.each_cons(2) do |ca_pair|
        expect(ca_pair.first < ca_pair.second).to be true
      end
    end

    it 'ascending' do
      media_entries_created_at('asc').each_cons(2) do |ca_pair|
        expect(ca_pair.first < ca_pair.second).to be true
      end
    end

    it 'descending' do
      media_entries_created_at('desc').each_cons(2) do |ca_pair|
        expect(ca_pair.first > ca_pair.second).to be true
      end
    end
  end
end
